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

def usd_to_brl(usd_value: float) -> float:
    try:
        response = requests.get("https://api.exchangerate-api.com/v4/latest/USD")
        data = response.json()
        rate = data["rates"]["BRL"]
        return round(usd_value * rate, 2)
    except Exception as e:
        print("Erro ao converter moeda:", e)
        return usd_value

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

UNKNOWN_RESPONSE = {
    "engine": "Unknown",
    "horsepower": 0,
    "torque": 0,
    "drivetrain": "Unknown",
    "price": 0
}

@app.post("/specs")
async def get_specs(request: VehicleRequest):
    prompt = f"""
You are an API that returns vehicle specifications.
Return ONLY a valid JSON object. No explanation. No markdown. No code blocks.

JSON format:
{{
    "engine": "string (ex: 2.0 Turbo 16V)",
    "horsepower": number (in HP),
    "torque": number (in Nm),
    "drivetrain": "string (FWD, RWD, AWD or 4WD)",
    "topSpeed": number (in km/h),
    "acceleration": number (0-100 km/h in seconds),
    "length": number (in meters),
    "width": number (in meters),
    "height": number (in meters),
    "weight": number (in kg),
    "electricRange": number (in km, use 0 if not electric),
    "price": number (in USD, launch price)
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
    "electricRange": 0,
    "price": 0
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
                data.get("torque", 0) <= 0 or
                data.get("price", 0) <= 0
        ):
            print("IA retornou dados inválidos")
            return UNKNOWN_RESPONSE

        price_usd = data.get("price", 0)
        if price_usd > 0:
            data["price"] = usd_to_brl(price_usd)

        return data

    except Exception as e:
        print("Erro na IA:", e)
        return UNKNOWN_RESPONSE



class CompareSummaryRequest(BaseModel):
    vehicleA: dict
    vehicleB: dict
    winner: str

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