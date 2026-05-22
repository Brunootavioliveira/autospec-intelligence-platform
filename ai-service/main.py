import os
import json
import requests
import google.generativeai as genai

from fastapi import FastAPI
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

app = FastAPI()

model = genai.GenerativeModel("gemini-2.5-flash-lite")


class VehicleRequest(BaseModel):
    brand: str
    model: str
    version: str
    year: int


class CompareSummaryRequest(BaseModel):
    vehicleA: dict
    vehicleB: dict
    winner: str


UNKNOWN_RESPONSE = {
    "engine": "Unknown",
    "horsepower": 0,
    "torque": 0,
    "drivetrain": "Unknown",
    "topSpeed": 0,
    "acceleration": 0,
    "length": 0,
    "width": 0,
    "height": 0,
    "weight": 0,
    "electricRange": 0,
    "price": 0
}


def extract_json(text: str):

    try:
        start = text.find("{")
        end = text.rfind("}") + 1

        json_str = text[start:end]

        return json.loads(json_str)

    except json.JSONDecodeError as e:
        print("Erro ao parsear JSON:", e)
        print("Texto recebido:", text)

        return None


def get_fipe_price(brand: str, model: str, year: int):

    try:

        brands_url = "https://parallelum.com.br/fipe/api/v1/carros/marcas"

        brands_response = requests.get(brands_url)

        brands = brands_response.json()

        brand_id = None

        for item in brands:

            if item["nome"].lower() == brand.lower():
                brand_id = item["codigo"]
                break

        if not brand_id:
            print("Marca não encontrada na FIPE")
            return 0

        models_url = (
            f"https://parallelum.com.br/fipe/api/v1/carros/"
            f"marcas/{brand_id}/modelos"
        )

        models_response = requests.get(models_url)

        models = models_response.json()["modelos"]

        model_id = None

        for item in models:

            if model.lower() in item["nome"].lower():
                model_id = item["codigo"]
                break

        if not model_id:
            print("Modelo não encontrado na FIPE")
            return 0

        years_url = (
            f"https://parallelum.com.br/fipe/api/v1/carros/"
            f"marcas/{brand_id}/modelos/{model_id}/anos"
        )

        years_response = requests.get(years_url)

        years = years_response.json()

        year_code = None

        for item in years:

            if str(year) in item["nome"]:
                year_code = item["codigo"]
                break

        if not year_code:
            print("Ano não encontrado na FIPE")
            return 0

        price_url = (
            f"https://parallelum.com.br/fipe/api/v1/carros/"
            f"marcas/{brand_id}/modelos/{model_id}/anos/{year_code}"
        )

        price_response = requests.get(price_url)

        price_data = price_response.json()

        value = price_data["Valor"]

        value = (
            value
            .replace("R$", "")
            .replace(".", "")
            .replace(",", ".")
            .strip()
        )

        return float(value)

    except Exception as e:

        print("Erro ao buscar preço FIPE:", e)

        return 0


@app.post("/specs")
async def get_specs(request: VehicleRequest):

    prompt = f"""
You are an API that returns vehicle specifications.

Return ONLY a valid JSON object.
No explanation.
No markdown.
No code blocks.

JSON format:
{{
    "engine": "string (ex: 2.0 Turbo 16V)",
    "horsepower": number,
    "torque": number,
    "drivetrain": "string (FWD, RWD, AWD or 4WD)",
    "topSpeed": number,
    "acceleration": number,
    "length": number,
    "width": number,
    "height": number,
    "weight": number,
    "electricRange": number
}}

If the vehicle does not exist or you are not certain, return exactly:
{{
    "engine": "Unknown",
    "horsepower": 0,
    "torque": 0,
    "drivetrain": "Unknown",
    "topSpeed": 0,
    "acceleration": 0,
    "length": 0,
    "width": 0,
    "height": 0,
    "weight": 0,
    "electricRange": 0
}}

Vehicle:
Brand: {request.brand}
Model: {request.model}
Version: {request.version}
Year: {request.year}
"""

    try:

        response = await model.generate_content_async(prompt)

        text = response.text.strip()

        print("RAW IA RESPONSE:\n", text)

        data = extract_json(text)

        if not data:
            raise Exception("Invalid JSON from AI")

        if (
                data.get("horsepower", 0) <= 0 or
                data.get("torque", 0) <= 0
        ):
            print("IA retornou dados inválidos")
            return UNKNOWN_RESPONSE

        fipe_price = get_fipe_price(
            request.brand,
            request.model,
            request.year
        )

        data["price"] = fipe_price

        return data

    except Exception as e:

        print("Erro na IA:", e)

        return UNKNOWN_RESPONSE


@app.post("/compare-summary")
async def compare_summary(request: CompareSummaryRequest):

    prompt = f"""
You are an automotive comparison assistant.

Generate ONLY a valid JSON object.

JSON format:
{{
    "summary": "string"
}}

Create a short professional comparison summary in Portuguese (Brazil).

The summary must:
- Mention strengths of both vehicles
- Mention performance, price and practicality when relevant
- Be concise (max 2 sentences)
- Sound natural and premium

Vehicle A:
{json.dumps(request.vehicleA, ensure_ascii=False)}

Vehicle B:
{json.dumps(request.vehicleB, ensure_ascii=False)}

Overall winner:
{request.winner}
"""

    try:

        response = await model.generate_content_async(prompt)

        text = response.text.strip()

        print("RAW SUMMARY RESPONSE:\n", text)

        data = extract_json(text)

        if not data:
            raise Exception("Invalid JSON from AI")

        return data

    except Exception as e:

        print("Erro ao gerar summary:", e)

        return {
            "summary": "Comparação realizada com sucesso entre os veículos."
        }