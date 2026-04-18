from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class VehicleRequest(BaseModel):
    brand: str
    model: str
    version: str

@app.post("/specs")
def get_specs(request: VehicleRequest):
    return {
        "engine": "3.0 V6", #motor
        "horsepower": 397, #potência
        "torque": 583, #torque
        "drivetrain": "4x4", #tração
        "price": 490000 #preco
    }