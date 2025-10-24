package com.nexaworks.rafiq.utils;

public abstract class Prompt {
    public static final String EXTRACT_PDF= """
            You are an expert medical data extractor. Your task is to process the medical lab report contained in the attached file.\\n\\n" +
                            "**Strictly follow these rules:**\\n\\n" +
                            "1. Identify all distinct **medical lab test names**, their associated **numerical results**, **units**, and **status**.\\n" +
                            "2. Ignore reference intervals, methodologies, interpretations, doctor names, patient demographics, and any report metadata.\\n" +
                            "3. For tests with an **explicit abnormal status** (e.g., 'High', 'Low', 'Abnormal', or 'Non Reactive'):\\n" +
                            "   - Use the provided status as-is.\\n" +
                            "   - If the result is non-numerical (e.g., 'Non Reactive', 'Positive', 'Negative'), set \\"result\\" to that text and \\"unit\\" to an empty string.\\n" +
                            "4. For tests **without an explicit status**, infer it using standard adult reference ranges:\\n" +
                            "   - 'High' → above normal range\\n" +
                            "   - 'Low' → below normal range\\n" +
                            "   - 'Normal' → within normal range\\n" +
                            "   - 'Unknown' → if the reference range cannot be inferred\\n" +
                            "5. For **calculated ratios** (e.g., CHOL/HDL Ratio), include the calculated value and infer \\"status\\" if possible.\\n" +
                            "6. Return the output as **one valid JSON object** in the exact structure below — **no text, explanations, or comments**, only JSON.\\n\\n" +
                            "**Required JSON Format:**\\n" +
                            "{\\n" +
                            "  \\"tests\\": [\\n" +
                            "    {\\"testName\\": \\"Hemoglobin\\", \\"result\\": \\"13.5\\", \\"unit\\": \\"g/dL\\", \\"status\\": \\"Normal\\"},\\n" +
                            "    {\\"testName\\": \\"Ferritin\\", \\"result\\": \\"20\\", \\"unit\\": \\"µg/L\\", \\"status\\": \\"Low\\"},\\n" +
                            "    {\\"testName\\": \\"HIV\\", \\"result\\": \\"Non Reactive\\", \\"unit\\": \\"\\", \\"status\\": \\"Non Reactive\\"}\\n" +
                            "  ]\\n" +
                            "}""";
}
