package com.nexaworks.rafiq.ai.utils;

public abstract class Prompt {
    public static final String EXTRACT_PDF = """
            You are an expert medical data extractor. Process the following medical lab report **exactly as specified**:


             **Strictly follow these rules:**

             1. Identify all distinct **medical lab test names**, their **numerical results**, **units**, and **status**. \s
             2. Ignore reference intervals, methodologies, interpretations, doctor names, patient demographics, and any report metadata. \s
             3. For tests with an **explicit abnormal status** (e.g., 'High', 'Low', 'Abnormal', 'Non Reactive'): \s
                - Use the provided status as-is. \s
                - If the result is non-numerical (e.g., 'Non Reactive', 'Positive', 'Negative'), set `"result"` to that text and `"unit"` to an empty string. \s
             4. For tests **without an explicit status**, infer it using standard adult reference ranges: \s
                - `"High"` → above normal range \s
                - `"Low"` → below normal range \s
                - `"Normal"` → within normal range \s
                - `"Unknown"` → if the reference range cannot be inferred \s
             5. For **calculated ratios** (e.g., CHOL/HDL Ratio), include the calculated value and infer `"status"` if possible. \s
             6. **Output ONLY JSON** in this exact structure — do **not** include explanations or any text outside the JSON:

             {
               "tests": [
                 {"testName": "Hemoglobin", "result": "13.5", "unit": "g/dL", "status": "Normal"},
                 {"testName": "Ferritin", "result": "20", "unit": "µg/L", "status": "Low"},
                 {"testName": "HIV", "result": "Non Reactive", "unit": "", "status": "Non Reactive"}
               ]
             }
            """;
    public static final String ANALYZE_LAB_RESULTS = """
            You are an expert medical analyst. Analyze the following medical lab test results and generate a comprehensive, patient-friendly report.

            **Input Data:**
            [PASTE THE EXTRACTED JSON DATA HERE]

            **Analysis Requirements:**

            1. **Overall Assessment**: Provide a clear summary of the patient's health status based on all test results.

            2. **Abnormal Findings**: Highlight all tests marked as "High", "Low", or "Abnormal" with:
               - What the test measures
               - Potential clinical significance
               - Common causes of abnormal values

            3. **Normal Findings**: Briefly acknowledge tests within normal range.

            4. **Health Implications**: Explain what the combination of results might indicate about:
               - Organ function (liver, kidney, heart, thyroid, etc.)
               - Nutritional status
               - Metabolic health
               - Infection or inflammation markers

            5. **Recommendations**: Suggest:
               - Which abnormal results require immediate medical attention
               - Lifestyle modifications that may help
               - Follow-up tests that may be needed

            6. **Important Disclaimer**: Include a clear statement that this is an AI-generated analysis and should not replace professional medical advice.

            **Output Format:**
            Return ONLY a JSON object with a single field called "report" containing the complete analysis as a formatted string with clear sections and line breaks for readability:

            {
              "report": "=== LAB RESULTS ANALYSIS ===\\n\\n**OVERALL ASSESSMENT:**\\n[Summary here]\\n\\n**ABNORMAL FINDINGS:**\\n[Details here]\\n\\n**NORMAL FINDINGS:**\\n[Details here]\\n\\n**HEALTH IMPLICATIONS:**\\n[Analysis here]\\n\\n**RECOMMENDATIONS:**\\n[Suggestions here]\\n\\n**IMPORTANT DISCLAIMER:**\\nThis analysis is AI-generated and for informational purposes only. Always consult with a qualified healthcare provider for medical advice, diagnosis, or treatment."
            }

            **Style Guidelines:**
            - Use clear, patient-friendly language
            - Avoid medical jargon where possible; explain technical terms when necessary
            - Be informative but not alarmist
            - Structure the report with clear headings and bullet points
            - Use \\n for line breaks and appropriate formatting within the JSON string
            """;
}
