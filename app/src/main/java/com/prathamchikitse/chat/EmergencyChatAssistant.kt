package com.prathamchikitse.chat

import com.prathamchikitse.domain.model.Emergency

/**
 * Offline rule-based first aid assistant.
 * Responds to keywords with pre-written guidance for the current emergency.
 * No internet required.
 */
object EmergencyChatAssistant {

    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    private data class Rule(
        val keywords: List<String>,
        val responseEn: String,
        val responseKn: String
    )

    private val generalRules = listOf(
        Rule(
            listOf("call", "phone", "emergency", "112", "108", "ambulance", "help"),
            "Call 112 (National Emergency) or 108 (Ambulance) immediately. These are toll-free numbers.",
            "112 (ರಾಷ್ಟ್ರೀಯ ತುರ್ತು) ಅಥವಾ 108 (ಆಂಬುಲೆನ್ಸ್) ಗೆ ತಕ್ಷಣ ಕರೆ ಮಾಡಿ. ಇವು ಉಚಿತ ಸಂಖ್ಯೆಗಳು."
        ),
        Rule(
            listOf("cpr", "chest compress", "heart pump", "resuscitat"),
            "CPR: Place hands on center of chest. Push hard and fast - 2 inches deep, 100-120 compressions per minute. Allow chest to fully recoil between compressions. Continue until help arrives.",
            "CPR: ಎದೆಯ ಮಧ್ಯಭಾಗದಲ್ಲಿ ಕೈಗಳನ್ನು ಇಡಿ. ಗಟ್ಟಿಯಾಗಿ ಮತ್ತು ವೇಗವಾಗಿ ಒತ್ತಿರಿ - 2 ಇಂಚು ಆಳ, ನಿಮಿಷಕ್ಕೆ 100-120 ಒತ್ತಡಗಳು."
        ),
        Rule(
            listOf("bleed", "blood", "wound", "cut"),
            "For bleeding: Apply firm direct pressure with clean cloth. Elevate the injured area above heart if possible. Don't remove the cloth - add more on top if it soaks through.",
            "ರಕ್ತಸ್ರಾವಕ್ಕೆ: ಸ್ವಚ್ಛವಾದ ಬಟ್ಟೆಯಿಂದ ಗಟ್ಟಿಯಾದ ಒತ್ತಡ ಹಾಕಿ. ಸಾಧ್ಯವಾದರೆ ಗಾಯವನ್ನು ಹೃದಯಕ್ಕಿಂತ ಮೇಲೆತ್ತಿ."
        ),
        Rule(
            listOf("breath", "breathe", "not breathing", "unconscious"),
            "If person is not breathing: Start CPR immediately. Call 112. Check for breathing every 2 minutes. If you are trained, give 2 rescue breaths after every 30 compressions.",
            "ವ್ಯಕ್ತಿ ಉಸಿರಾಡದಿದ್ದರೆ: ತಕ್ಷಣ CPR ಪ್ರಾರಂಭಿಸಿ. 112 ಗೆ ಕರೆ ಮಾಡಿ."
        ),
        Rule(
            listOf("burn", "fire", "hot", "scald"),
            "For burns: Run cool water over the burn for 10-20 minutes. Don't use ice. Cover with clean cloth. Don't pop blisters or apply butter/ointments.",
            "ಸುಟ್ಟ ಗಾಯಕ್ಕೆ: 10-20 ನಿಮಿಷಗಳ ಕಾಲ ತಂಪಾದ ನೀರು ಹರಿಸಿ. ಐಸ್ ಬಳಸಬೇಡಿ. ಸ್ವಚ್ಛ ಬಟ್ಟೆಯಿಂದ ಮುಚ್ಚಿ."
        ),
        Rule(
            listOf("choke", "stuck", "throat", "can't breathe", "swallow"),
            "For choking: Stand behind person. Make fist above navel. Pull inward and upward sharply 5 times (Heimlich maneuver). If person becomes unconscious, start CPR.",
            "ಉಸಿರುಗಟ್ಟುವಿಕೆಗೆ: ವ್ಯಕ್ತಿಯ ಹಿಂದೆ ನಿಲ್ಲಿ. ನಾಭಿಗಿಂತ ಮೇಲೆ ಮುಷ್ಠಿ ಮಾಡಿ. 5 ಬಾರಿ ಒಳಗೆ ಮತ್ತು ಮೇಲೆ ಎಳೆಯಿರಿ."
        ),
        Rule(
            listOf("fracture", "bone", "break", "sprain", "joint"),
            "For fractures: Don't try to realign the bone. Immobilize the area with a splint. Apply ice pack wrapped in cloth. Seek medical help.",
            "ಮೂಳೆ ಮುರಿತಕ್ಕೆ: ಮೂಳೆಯನ್ನು ಮರುಸರಿಪಡಿಸಲು ಪ್ರಯತ್ನಿಸಬೇಡಿ. ಸ್ಪ್ಲಿಂಟ್‌ನಿಂದ ಸ್ಥಿರಗೊಳಿಸಿ."
        ),
        Rule(
            listOf("fever", "temperature", "hot body", "heat"),
            "For fever: Check temperature. Above 103°F (adults) or 100.4°F (children) needs medical attention. Give paracetamol. Keep hydrated. Don't use cold baths.",
            "ಜ್ವರಕ್ಕೆ: ತಾಪಮಾನ ಪರಿಶೀಲಿಸಿ. ಪ್ಯಾರಾಸಿಟಮಾಲ್ ನೀಡಿ. ದ್ರವ ಪದಾರ್ಥ ಸೇವಿಸಿ."
        ),
        Rule(
            listOf("aspirin", "medicine", "medication", "tablet", "pill"),
            "For heart attack: If person is conscious and not allergic, give 300mg aspirin to chew. Don't give aspirin to children or if allergic.",
            "ಹೃದಯಾಘಾತಕ್ಕೆ: ವ್ಯಕ್ತಿ ಪ್ರಜ್ಞಾವಂತರಾಗಿದ್ದರೆ ಮತ್ತು ಅಲರ್ಜಿ ಇಲ್ಲದಿದ್ದರೆ, 300mg ಆಸ್ಪ್ರಿನ್ ನೀಡಿ."
        ),
        Rule(
            listOf("thank", "thanks"),
            "You're welcome! Stay safe. If the situation is serious, call 112 immediately.",
            "ಸ್ವಾಗತ! ಸುರಕ್ಷಿತವಾಗಿರಿ. ಪರಿಸ್ಥಿತಿ ಗಂಭೀರವಾಗಿದ್ದರೆ ತಕ್ಷಣ 112 ಗೆ ಕರೆ ಮಾಡಿ."
        ),
        Rule(
            listOf("hello", "hi", "hey"),
            "Hello! I'm your First Aid Assistant. Ask me anything about the emergency you're dealing with. You can also type keywords like 'cpr', 'bleeding', 'burns', etc.",
            "ನಮಸ್ಕಾರ! ನಾನು ನಿಮ್ಮ ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ ಸಹಾಯಕ. ತುರ್ತು ಪರಿಸ್ಥಿತಿಯ ಬಗ್ಗೆ ಏನಾದರೂ ಕೇಳಿ."
        ),
        Rule(
            listOf("pressure", "press", "push"),
            "Apply firm, continuous pressure on the wound using a clean cloth or bandage. Don't stop pressing - keep pressure constant until bleeding stops or help arrives.",
            "ಸ್ವಚ್ಛ ಬಟ್ಟೆ ಅಥವಾ ಬ್ಯಾಂಡೇಜ್ ಬಳಸಿ ಗಾಯದ ಮೇಲೆ ಗಟ್ಟಿಯಾದ ಒತ್ತಡ ಹಾಕಿ. ಒತ್ತಡವನ್ನು ನಿಲ್ಲಿಸಬೇಡಿ."
        ),
        Rule(
            listOf("ice", "cold", "pack"),
            "For ice pack: Wrap ice in a cloth - never apply ice directly to skin. Apply for 15-20 minutes. This helps reduce swelling and pain for fractures and sprains.",
            "ಐಸ್ ಪ್ಯಾಕ್: ಐಸ್ ಅನ್ನು ಬಟ್ಟೆಯಲ್ಲಿ ಸುತ್ತಿ - ನೇರವಾಗಿ ಚರ್ಮಕ್ಕೆ ಹಾಕಬೇಡಿ. 15-20 ನಿಮಿಷ ಹಾಕಿ."
        ),
        Rule(
            listOf("position", "lie", "sit", "stand", "sleep"),
            "Position matters: For heart attack, help person sit semi-upright. For fainting, lay them flat with legs elevated. For breathing problems, sitting up is best. Don't move if spinal injury suspected.",
            "ಸ್ಥಾನ ಮುಖ್ಯ: ಹೃದಯಾಘಾತಕ್ಕೆ ಅರ್ಧ-ನಿಂತ ಸ್ಥಿತಿ. ಮೂರ್ಛೆಗೆ ಮಲಗಿಸಿ ಕಾಲುಗಳನ್ನು ಎತ್ತಿ."
        ),
    )

    private fun findResponse(query: String, language: String): String {
        val lowerQuery = query.lowercase()

        for (rule in generalRules) {
            for (keyword in rule.keywords) {
                if (lowerQuery.contains(keyword.lowercase())) {
                    return if (language == "kn") rule.responseKn else rule.responseEn
                }
            }
        }

        return if (language == "kn")
            "ಕ್ಷಮಿಸಿ, ನನಗೆ ಆ ಪ್ರಶ್ನೆಗೆ ಉತ್ತರ ತಿಳಿಯದು. ತುರ್ತು ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ 112 ಗೆ ಕರೆ ಮಾಡಿ."
        else
            "I don't have specific guidance for that. Focus on the steps shown above, and call 112 if the situation is serious."
    }

    fun getResponse(query: String, language: String): ChatMessage {
        return ChatMessage(text = findResponse(query, language), isUser = false)
    }

    fun getWelcomeMessage(emergency: Emergency, language: String): ChatMessage {
        val title = if (language == "kn") emergency.titleKn else emergency.titleEn
        return if (language == "kn")
            ChatMessage(
                text = "ನಮಸ್ಕಾರ! ನಾನು ನಿಮ್ಮ ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ ಸಹಾಯಕ. \"$title\" ಬಗ್ಗೆ ನಿಮಗೆ ಸಹಾಯ ಮಾಡುತ್ತೇನೆ.\n\nನೀವು ಇವುಗಳನ್ನು ಕೇಳಬಹುದು:\n• CPR ಹೇಗೆ ಮಾಡಬೇಕು\n• ರಕ್ತಸ್ರಾವ ನಿಲ್ಲಿಸುವುದು ಹೇಗೆ\n• ಔಷಧಿ ಮಾಹಿತಿ\n• ಸರಿಯಾದ ಸ್ಥಾನ\n\nನಿಮ್ಮ ಪ್ರಶ್ನೆಯನ್ನು ಟೈಪ್ ಮಾಡಿ.",
                isUser = false
            )
        else
            ChatMessage(
                text = "Hello! I'm your First Aid Assistant for \"$title\".\n\nYou can ask me about:\n• How to perform CPR\n• How to stop bleeding\n• Medicine information\n• Correct positioning\n\nType your question below.",
                isUser = false
            )
    }
}
