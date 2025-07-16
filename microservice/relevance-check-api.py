from fastapi import FastAPI, Request, status, Body
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
import uvicorn
import requests
import json


app = FastAPI()
model = SentenceTransformer("all-MiniLM-L6-v2")

# =============================
# 📘 Pydantic Model
# =============================
class RelevanceInput(BaseModel):
    prompt: str
    response: str

# =============================
# ✅ Main Endpoint with Confidence
# =============================
@app.post("/check-relevance")
async def check_relevance(payload: RelevanceInput = Body(...)):
    print("📥 [check-relevance] Parsed Body:", payload)

    embeddings = model.encode([payload.prompt, payload.response])
    score = util.cos_sim(embeddings[0], embeddings[1]).item()

    # Confidence levels
    if score >= 0.8:
        confidence = "high"
    elif score >= 0.6:
        confidence = "medium"
    else:
        confidence = "low"

    return {
        "relevance_score": round(score, 4),
        "is_relevant": score >= 0.6,
        "confidence": confidence
    }

@app.post("/check-relevance-mistral")
def check_relevance_with_mistral(input: RelevanceInput):
    eval_prompt = f"""
You are a semantic evaluator. Given a user prompt and a chatbot response, analyze how relevant the chatbot's reply is.

Return the following **strict JSON object**:

{{
  "relevance_score": float between 0 and 1,
  "is_relevant": boolean (true if relevance_score >= 0.6),
  "reasoning": string explaining why the score was assigned
}}

User Prompt: {input.prompt}
Chatbot Reply: {input.response}
""".strip()

    response = requests.post(
        "http://localhost:11434/api/generate",
        json={"model": "mistral", "prompt": eval_prompt},
        stream=True
    )

    full_response = ""
    for line in response.iter_lines():
        if line:
            try:
                chunk = json.loads(line.decode("utf-8"))
                if "response" in chunk:
                    full_response += chunk["response"]
            except Exception:
                continue

    try:
        parsed = json.loads(full_response.strip())
        return parsed
    except Exception as e:
        return {
            "message": "Failed to parse Mistral output.",
            "raw_output": full_response
        }


# =============================
# 🧪 Debug Endpoint
# =============================
@app.post("/debug")
async def debug_payload(request: Request):
    body = await request.body()
    return {"raw_body": body.decode(errors="replace")}

# =============================
# 🚨 Global 422 Handler
# =============================
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    body = await request.body()
    body_decoded = body.decode(errors="replace")

    print("🚨 Validation Error (422)")
    print("🔹 Request URL:", request.url.path)
    print("🔹 Headers:", dict(request.headers))
    print("🔹 Raw Body:", body_decoded)
    print("🔹 Validation Details:", exc.errors())

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "message": "Validation failed",
            "detail": exc.errors(),
            "raw_body": body_decoded
        }
    )

# =============================
# 🚀 Launch
# =============================
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
