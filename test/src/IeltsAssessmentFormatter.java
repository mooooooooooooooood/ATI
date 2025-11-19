import org.json.JSONObject;

public class IeltsAssessmentFormatter {

    public static void main(String[] args) {
        String rawJson = "{\n" +
                "  \"overall_band\": 6.0,\n" +
                "  \"fluency_and_coherence\": {\n" +
                "    \"band\": 7.0,\n" +
                "    \"assessment\": \"The candidate's fluency is good, but there are moments where she struggles to maintain a consistent pace. She occasionally uses filler words such as 'um' and 'like'. Her vocabulary is decent, and she uses a range of words to express herself.\\n\\nA notable example is a long, run-on explanation where she describes how her mother began with a modeling job, later became a director of finance, built office buildings and a house by herself, and succeeded despite lacking a formal educational background. She continues to elaborate that her mother is knowledgeable, opinionated, assertive, and highly inspiring. She also describes her mother’s difficult childhood during the war in Sri Lanka, her resilience, her achievements, and her dedication as a working mother who remained heavily involved in her children’s upbringing.\\n\\nWhile coherent, the long, uninterrupted stretches of speech reduce clarity and make the answer sound less controlled.\"\n" +
                "  },\n" +
                "  \"lexical_resource\": {\n" +
                "    \"band\": 6.0,\n" +
                "    \"assessment\": \"The candidate demonstrates adequate vocabulary but often relies on general or repetitive expressions. A large portion of her response consists of a long, unstructured narrative with repeated phrases such as'she started','she built','she made sure', and'she never let anything stop her'.\\n\\nDespite demonstrating admiration effectively, the language lacks precision and relies heavily on everyday expressions. More topic-specific vocabulary, paraphrasing, and a wider lexical range would strengthen her score.\"\n" +
                "  },\n" +
                "  \"grammatical_range_accuracy\": {\n" +
                "    \"band\": 6.5,\n" +
                "    \"assessment\": \"The candidate uses mostly simple or loosely connected sentence structures. Much of her response is one extended run-on sentence linked by 'and', 'but', and'so'. Although grammar is generally understandable, the lack of controlled complex structures limits accuracy.\\n\\nSome clauses are repeated or blended together without punctuation, reducing clarity. More varied sentence forms and clearer segmentation would improve grammatical control.\"\n" +
                "  },\n" +
                "  \"pronunciation\": {\n" +
                "    \"band\": 6.0,\n" +
                "    \"assessment\": \"The candidate's pronunciation is generally understandable, though she occasionally struggles with articulation and rhythm. Some words are slightly unclear, and connected speech sometimes becomes muddled due to long, uninterrupted sentences. However, her accent is intelligible, and meaning remains clear.\\n\\nShe also uses a mildly formal tone, which aligns with her speaking style. Improved articulation, pausing, and intonation control would help raise her score.\"\n" +
                "  }\n" +
                "}";

        // Parse the JSON
        JSONObject json = new JSONObject(rawJson);

        System.out.println("Overall Band Score: " + json.getDouble("overall_band") + "\n");

        printSection("Fluency & Coherence", json.getJSONObject("fluency_and_coherence"));
        printSection("Lexical Resource", json.getJSONObject("lexical_resource"));
        printSection("Grammatical Range & Accuracy", json.getJSONObject("grammatical_range_accuracy"));
        printSection("Pronunciation", json.getJSONObject("pronunciation"));
    }

    private static void printSection(String title, JSONObject section) {
        System.out.println("=== " + title + " (Band: " + section.getDouble("band") + ") ===");
        String assessment = section.getString("assessment").replaceAll("\\\\n\\\\n", "\n\n"); // format line breaks
        System.out.println(assessment + "\n");
    }
}
