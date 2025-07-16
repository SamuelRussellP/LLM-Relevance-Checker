from fastapi import FastAPI, Request, status, Body
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from pydantic import BaseModel
import requests
import json
import uvicorn

app = FastAPI()

# =============================
# 📘 Input Model (Prompt + Response)
# =============================
class JobRelevanceInput(BaseModel):
    message: str
    chatbot_response: str

# =============================
# ✅ Endpoint: Job Application Evaluation
# =============================
@app.post("/check-relevance-job")
def check_relevance_job(input: JobRelevanceInput):
    eval_prompt = f"""
You are a semantic evaluator. Based on the user prompt and chatbot response, determine:

- A "relevance_score" from 0 to 1 for how appropriate the chatbot response is.
- A "job_application_completed" boolean: true if it indicates the user has finished the job application.
- A "reasoning" string explaining the evaluation.

Return strictly in this format:
{{
  "relevance_score": 0.85,
  "job_application_completed": true,
  "reasoning": "..."
}}

Message:
{input.message}

Chatbot Response:
{input.chatbot_response}
""".strip()

    # Mistral API via Ollama
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
            except:
                continue

    try:
        return json.loads(full_response.strip())
    except Exception:
        return {
            "message": "❌ Failed to parse JSON from model.",
            "raw_output": full_response
        }

# =============================
# 🚨 Validation Error Handler
# =============================
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    body = await request.body()
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "message": "Validation failed",
            "detail": exc.errors(),
            "raw_body": body.decode(errors="replace")
        }
    )

# =============================
# 🚀 Run API
# =============================
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8002)
