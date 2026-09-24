package com.example.data.repository

import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression

object DialectCatalog {

    val dialects: List<RegionalDialect> = listOf(
        // 1. Kozhikode Malayalam
        RegionalDialect(
            id = "ml_in_kl_kozhikode",
            language = "Malayalam",
            country = "India",
            region = "Kerala",
            cityOrArea = "Kozhikode",
            dialectName = "Kozhikode Slang",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "എന്തൊക്കെയുണ്ട് ചങ്ങായി വിശേഷങ്ങൾ?",
                "ഒരു നല്ല സുലൈമാനി കുടിച്ചാലോ?",
                "ഓൻ നല്ലോണം സംസാരിക്കും ട്ടോ",
                "കാര്യം നൈസ് ആയിട്ട് കൈകാര്യം ചെയ്തു"
            ),
            greeting = "എന്താപ്പാ ചങ്ങായി വിശേഷം? സുഖല്ലേ? ഒരു സുലൈമാനി കുടിച്ച് സംസാരിക്കാം!",
            localeCode = "ml-IN",
            description = "Warm, musical, Malabar tea-shop conversational cadence with famous affectionate slang and friendly contractions.",
            codeSwitchingDescription = "Mixes smoothly with English ('Manglish') for tech, work, and casual banter.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ചങ്ങായി (Changayi)",
                    meaning = "Dear close friend, companion or pal",
                    region = "Kozhikode, Kerala",
                    dialect = "Kozhikode Slang",
                    context = "Used affectionately when greeting or referring to friends in everyday talks",
                    formalEquivalent = "സുഹൃത്ത് (Suhrith)",
                    exampleSentence = "നമ്മുടെ ചങ്ങായി വന്നിട്ടുണ്ട്, ഒരു സുലൈമാനി കൊടുക്ക്!",
                    toneCategory = "Friendly",
                    culturalNotes = "Quintessential North Malabar warmth symbolizing brotherhood and deep community hospitality."
                ),
                RegionalExpression(
                    expression = "സുലൈമാനി (Sulaimani)",
                    meaning = "Spiced black tea with cardamom and lemon",
                    region = "Kozhikode, Kerala",
                    dialect = "Kozhikode Slang",
                    context = "The centerpiece of Kozhikode conversations, bonding, and deep discussions",
                    formalEquivalent = "കട്ടൻ ചായ (Black tea)",
                    exampleSentence = "ചൂട് ബിരിയാണിക്ക് ശേഷം ഒരു സുലൈമാനി നിർബന്ധമാണ്.",
                    toneCategory = "Affectionate",
                    culturalNotes = "Celebrated across Kozhikode culture and movies as the spirit of heartfelt hospitality."
                ),
                RegionalExpression(
                    expression = "ഓൻ / ഓള് (On / Olu)",
                    meaning = "He / She (casual third person pronoun)",
                    region = "Kozhikode, Kerala",
                    dialect = "Kozhikode Slang",
                    context = "Common natural pronoun in Kozhikode and Malabar instead of formal അവൻ/അവൾ",
                    formalEquivalent = "അവൻ / അവൾ (Avan / Aval)",
                    exampleSentence = "ഓള് നാളെ കടപ്പുറത്തേക്ക് വരും എന്ന് പറഞ്ഞിട്ടുണ്ട്.",
                    toneCategory = "Casual",
                    culturalNotes = "Distinctive phonological softening characteristic of coastal northern Kerala."
                ),
                RegionalExpression(
                    expression = "അങ്ങട് വര്യ്വോ (Angadu Varyvo)",
                    meaning = "Will you come over? / Come this way",
                    region = "Kozhikode, Kerala",
                    dialect = "Kozhikode Slang",
                    context = "Warm invitation to visit a shop, house, or hangout",
                    formalEquivalent = "ഇങ്ങോട്ട് വരുമോ (Ingottu varumo)",
                    exampleSentence = "വൈകുന്നേരം ബീച്ചിലേക്ക് അങ്ങട് വര്യ്വോ ചങ്ങായി?",
                    toneCategory = "Warm",
                    culturalNotes = "Features the elongated 'yo' interrogative ending unique to Kozhikode speech."
                )
            )
        ),

        // 2. Thrissur Malayalam
        RegionalDialect(
            id = "ml_in_kl_thrissur",
            language = "Malayalam",
            country = "India",
            region = "Kerala",
            cityOrArea = "Thrissur",
            dialectName = "Thrissur Slang",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "ഗഡീ, പിന്നെന്തൂട്ടാ കഥ?",
                "നമ്മളെന്തൂട്ടാ ഇപ്പൊ ചെയ്യാ?",
                "പൂരത്തിന്റെ വൈബ് തകർത്തു ട്ടാ",
                "മച്ചൂ, അതൊരു സംഭവാണ് ട്ടോ"
            ),
            greeting = "എന്തൂട്ടാ ഗഡീ വിശേഷങ്ങൾ? തകർപ്പൻ മൂഡിലാണോ?",
            localeCode = "ml-IN",
            description = "Famous for rhythmic pitch inflections, the signature question marker 'Enthoottu', and energetic communal banter.",
            codeSwitchingDescription = "Mixes college campus Manglish with heavy Thrissur melodic vowels.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ഗഡീ (Gadi)",
                    meaning = "Pal, mate, bro",
                    region = "Thrissur, Kerala",
                    dialect = "Thrissur Slang",
                    context = "Addressed to peers, friends, and trusted comrades",
                    formalEquivalent = "കൂട്ടുകാരൻ (Koottukaran)",
                    exampleSentence = "നമ്മുടെ ഗഡീ വന്നിട്ടുണ്ട്, ഒരു റൗണ്ട് കറങ്ങാം!",
                    toneCategory = "Casual",
                    culturalNotes = "Thrissur's iconic informal address used across colleges, tea stalls, and the Round."
                ),
                RegionalExpression(
                    expression = "എന്തൂട്ടാ (Enthoottu / Enthoottu-katha)",
                    meaning = "What on earth? / What is it?",
                    region = "Thrissur, Kerala",
                    dialect = "Thrissur Slang",
                    context = "Used constantly as an inquisitive filler or to express mild pleasant disbelief",
                    formalEquivalent = "എന്താണ് (Endhanu)",
                    exampleSentence = "എന്തൂട്ടാ ഗഡീ നീ ഈ പറയുന്നത്?",
                    toneCategory = "Humorous",
                    culturalNotes = "The defining auditory signature that instantly reveals a speaker from Thrissur district."
                )
            )
        ),

        // 3. Malappuram Malayalam
        RegionalDialect(
            id = "ml_in_kl_malappuram",
            language = "Malayalam",
            country = "India",
            region = "Kerala",
            cityOrArea = "Malappuram",
            dialectName = "Malappuram Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "സുഖല്ലേ തങ്ങൾക്ക്?",
                "നല്ല മൊഞ്ചുള്ള പരിപാടി",
                "ന്തൂട്ടാപ്പാ ഇത്ര ധൃതി?",
                "കുപ്പായം തുന്നാൻ കൊടുത്തോ?"
            ),
            greeting = "അസ്സലാമു അലൈക്കും ചങ്ങായി! എന്തൊക്കെയുണ്ട് വിശേഷങ്ങൾ? സുഖല്ലേ?",
            localeCode = "ml-IN",
            description = "Lyrical, soft-toned, incorporating classic Arabi-Malayalam linguistic heritage, football craze idioms, and tender respect.",
            codeSwitchingDescription = "Infuses traditional terms with modern youth slang and football vocabulary.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "മൊഞ്ചൻ / മൊഞ്ചുള്ള (Monjan / Monjulla)",
                    meaning = "Very handsome, charming, or beautiful",
                    region = "Malappuram, Kerala",
                    dialect = "Malappuram Style",
                    context = "Complimenting people, outfits, food, or aesthetics",
                    formalEquivalent = "സുന്ദരൻ / ഭംഗിയുള്ള (Sundaran)",
                    exampleSentence = "ഇന്നത്തെ ഫുട്ബോൾ കളി കാണാൻ നല്ല മൊഞ്ചുണ്ടായിരുന്നു!",
                    toneCategory = "Affectionate",
                    culturalNotes = "Originating from Arabic 'Manzar' (sight/view), a cherished praise word in Malappuram."
                )
            )
        ),

        // 4. Liverpool Scouse English
        RegionalDialect(
            id = "en_gb_eng_liverpool",
            language = "English",
            country = "United Kingdom",
            region = "England",
            cityOrArea = "Liverpool",
            dialectName = "Liverpool Scouse",
            flagEmoji = "🇬🇧",
            samplePhrases = listOf(
                "Alright mate, you sound?",
                "That gig last night was proper boss!",
                "I'm starving, let's grab some scran.",
                "Devo'd about the footy score, lad."
            ),
            greeting = "Alright kidda! How's it going lad? You sound? Let's have a proper chinwag.",
            localeCode = "en-GB",
            description = "Famous melodious northern cadence with distinct glottal stops, unique vocabulary, and warm quick-witted banter.",
            codeSwitchingDescription = "Standard British colloquial English seamlessly blended with Merseyside slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Sound",
                    meaning = "Good, fine, reliable, cool, or genuine",
                    region = "Liverpool, Merseyside",
                    dialect = "Liverpool Scouse",
                    context = "Used to describe a great person, confirm agreement, or ask how someone is doing",
                    formalEquivalent = "Good / Excellent / Dependable",
                    exampleSentence = "He's a sound lad, helped me fix the wheel no questions asked.",
                    toneCategory = "Friendly",
                    culturalNotes = "One of the most versatile positive affirmations in Scouse vernacular."
                ),
                RegionalExpression(
                    expression = "Boss",
                    meaning = "Brilliant, incredible, top quality",
                    region = "Liverpool, Merseyside",
                    dialect = "Liverpool Scouse",
                    context = "Celebrating something wonderful or very high quality",
                    formalEquivalent = "Outstanding / Fantastic",
                    exampleSentence = "That meal we had down the Baltic Triangle was absolute boss!",
                    toneCategory = "Slang",
                    culturalNotes = "Commonly used across all generations in Liverpool."
                ),
                RegionalExpression(
                    expression = "Scran",
                    meaning = "Food or a hearty meal",
                    region = "Liverpool, Merseyside",
                    dialect = "Liverpool Scouse",
                    context = "When hungry or discussing where to eat",
                    formalEquivalent = "Food / Cuisine",
                    exampleSentence = "Proper starving here, anyone fancy getting some decent scran?",
                    toneCategory = "Casual",
                    culturalNotes = "Originally naval slang adopted permanently into North West English dialect."
                ),
                RegionalExpression(
                    expression = "Devo'd",
                    meaning = "Devastated, heartbroken, deeply upset",
                    region = "Liverpool, Merseyside",
                    dialect = "Liverpool Scouse",
                    context = "Expressing disappointment when your football team loses or plans fall through",
                    formalEquivalent = "Disappointed / Devastated",
                    exampleSentence = "Devo'd we didn't get tickets for the Anfield match, lad.",
                    toneCategory = "Casual",
                    culturalNotes = "Shortened phonetic contraction typical of quick Liverpool speech."
                )
            )
        ),

        // 5. NYC Brooklyn English
        RegionalDialect(
            id = "en_us_ny_brooklyn",
            language = "English",
            country = "United States",
            region = "New York",
            cityOrArea = "Brooklyn",
            dialectName = "NYC / Brooklyn Style",
            flagEmoji = "🇺🇸",
            samplePhrases = listOf(
                "Deadass, that pizza place is mad good.",
                "Running down to the corner bodega real quick.",
                "That's straight facts, no cap.",
                "Yo, what's good with you today?"
            ),
            greeting = "Yo, what's good! How you living? Pull up a chair and let's talk facts.",
            localeCode = "en-US",
            description = "Fast-paced, direct, street-smart New York cadence with iconic borough phrases and energetic confidence.",
            codeSwitchingDescription = "Mixes hip-hop idioms, multicultural New York expressions, and fast metro cadence.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Deadass",
                    meaning = "Completely serious, truthful, not exaggerating",
                    region = "New York City, NY",
                    dialect = "NYC / Brooklyn Style",
                    context = "Emphasizing absolute honesty or affirming truth",
                    formalEquivalent = "In all seriousness / Genuinely",
                    exampleSentence = "I'm deadass right now, that was the best espresso I ever had in the borough.",
                    toneCategory = "Street Slang",
                    culturalNotes = "Born in NYC street vernacular, now globally recognized but fiercely New York in spirit."
                ),
                RegionalExpression(
                    expression = "Bodega",
                    meaning = "Corner neighborhood convenience store & deli",
                    region = "New York City, NY",
                    dialect = "NYC / Brooklyn Style",
                    context = "The go-to neighborhood hub for coffee, snacks, chopped cheese, and gossip",
                    formalEquivalent = "Convenience store",
                    exampleSentence = "Grab me a BEC from the bodega while you're down there.",
                    toneCategory = "Casual",
                    culturalNotes = "From Spanish for storeroom/wine cellar, representing the vibrant heartbeat of NYC communities."
                ),
                RegionalExpression(
                    expression = "Mad (as modifier)",
                    meaning = "Extremely, remarkably, very",
                    region = "New York City, NY",
                    dialect = "NYC / Brooklyn Style",
                    context = "Amplifier preceding adjectives",
                    formalEquivalent = "Very / Extremely",
                    exampleSentence = "The train was mad delayed this morning on the Q line.",
                    toneCategory = "Casual",
                    culturalNotes = "A timeless NYC staple used to intensify everything from hunger to excitement."
                )
            )
        ),

        // 6. Melbourne Australian English
        RegionalDialect(
            id = "en_au_vic_melbourne",
            language = "English",
            country = "Australia",
            region = "Victoria",
            cityOrArea = "Melbourne",
            dialectName = "Melburnian Aussie",
            flagEmoji = "🇦🇺",
            samplePhrases = listOf(
                "G'day mate, how's it going?",
                "Grabbing some brekkie and a flat white down the laneway.",
                "No worries at all, too easy!",
                "Catch ya this arvo for a kick of the footy."
            ),
            greeting = "G'day mate! How're ya travelling? Keen for a proper yarn over a good coffee?",
            localeCode = "en-AU",
            description = "Relaxed rising inflection, famous Aussie diminutive abbreviations, world-famous coffee snobbery, and laid-back warmth.",
            codeSwitchingDescription = "Easily transitions between international business English and relaxed Australian bush/city slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "No worries",
                    meaning = "You're welcome / It's all good / No problem at all",
                    region = "Melbourne, Victoria",
                    dialect = "Melburnian Aussie",
                    context = "Standard friendly response in virtually every social interaction",
                    formalEquivalent = "You are most welcome",
                    exampleSentence = "Thanks for giving us a hand with the bags! — No worries mate!",
                    toneCategory = "Friendly",
                    culturalNotes = "The cultural emblem of Australian easygoing optimism and mateship."
                ),
                RegionalExpression(
                    expression = "Brekkie",
                    meaning = "Breakfast",
                    region = "Melbourne, Victoria",
                    dialect = "Melburnian Aussie",
                    context = "Referring to the morning meal, especially brunch culture",
                    formalEquivalent = "Breakfast",
                    exampleSentence = "Smashed avo on sourdough for brekkie is undefeated.",
                    toneCategory = "Casual",
                    culturalNotes = "Aussie English famously shortens nouns and adds '-ie' or '-o'."
                )
            )
        ),

        // 7. Kuwaiti Arabic
        RegionalDialect(
            id = "ar_kw_kuwait",
            language = "Arabic",
            country = "Kuwait",
            region = "Kuwait",
            cityOrArea = "Kuwait City",
            dialectName = "Kuwaiti Gulf Arabic",
            flagEmoji = "🇰🇼",
            samplePhrases = listOf(
                "شلونك يا معود؟ عساك طيب؟",
                "والله المكان وايد زين وحلو",
                "حياك الله في ديوانيتنا الليلة",
                "ما تقصر يا خوي، كفيت ووفيت"
            ),
            greeting = "هلا والله! شلونك يا معود؟ عساك بخير وصحة؟ حياك الله تفضل!",
            localeCode = "ar-KW",
            description = "Warm Gulf cadence with distinctive 'ch' sounds for kaf, iconic Gulf vocabulary ('wayed', 'ya m'awwad'), and rich Diwaniya hospitality.",
            codeSwitchingDescription = "Frequently combines Gulf Arabic with English business & everyday terms ('Arabizi' & Gulf code-switching).",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "شلونك (Shlonak)",
                    meaning = "How are you? (literally: What is your color?)",
                    region = "Kuwait & Gulf",
                    dialect = "Kuwaiti Gulf Arabic",
                    context = "Universal greeting when meeting anyone",
                    formalEquivalent = "كيف حالك (Kayfa haluk)",
                    exampleSentence = "شلونك يا بو فهد؟ عساك على القوة!",
                    toneCategory = "Friendly",
                    culturalNotes = "An ancient idiom asking after someone's complexion/vitality, now the core Gulf greeting."
                ),
                RegionalExpression(
                    expression = "وايد (Wayed)",
                    meaning = "A lot, very, extremely",
                    region = "Kuwait",
                    dialect = "Kuwaiti Gulf Arabic",
                    context = "Expressing quantity or degree",
                    formalEquivalent = "كثيراً / جداً (Katheeran)",
                    exampleSentence = "الجو اليوم وايد بارد وحلو بالبر.",
                    toneCategory = "Casual",
                    culturalNotes = "Distinctive Gulf Arabic word known throughout the Arab world."
                ),
                RegionalExpression(
                    expression = "يا معود (Ya M'awwad)",
                    meaning = "Come on man! / You've got to be kidding / Relax buddy",
                    region = "Kuwait",
                    dialect = "Kuwaiti Gulf Arabic",
                    context = "Used affectionately or during lively banter and storytelling",
                    formalEquivalent = "يا أخي / يا رجل",
                    exampleSentence = "يا معود لا تحاتي، الموضوع بسيط وبنخلصه باكر.",
                    toneCategory = "Humorous",
                    culturalNotes = "Iconic Kuwaiti catchphrase representing friendly camaraderie."
                ),
                RegionalExpression(
                    expression = "ما تقصر (Ma Tqassir)",
                    meaning = "You've been incredibly generous / Thank you so much",
                    region = "Kuwait",
                    dialect = "Kuwaiti Gulf Arabic",
                    context = "Thanking someone for their help, hospitality, or gift",
                    formalEquivalent = "شكراً جزيلاً لك",
                    exampleSentence = "تسلم على العزيمة وما تقصر يا بو علي.",
                    toneCategory = "Affectionate",
                    culturalNotes = "High-etiquette expression meaning 'you never fall short in honor'."
                )
            )
        ),

        // 8. Cairene Egyptian Arabic
        RegionalDialect(
            id = "ar_eg_cairo",
            language = "Arabic",
            country = "Egypt",
            region = "Cairo",
            cityOrArea = "Cairo",
            dialectName = "Cairene Egyptian",
            flagEmoji = "🇪🇬",
            samplePhrases = listOf(
                "عامل إيه يا باشا؟ كله تمام؟",
                "الموضوع ده زي الفل ومفيهوش مشاكل",
                "قشطة عليك يا فنان، نتقابل بكرة",
                "أنا بحبك أوي يا صاحبي"
            ),
            greeting = "يا مساء الفل! إزيك يا باشا؟ عامل إيه في يومك؟",
            localeCode = "ar-EG",
            description = "The universally understood dialect of Egyptian cinema, vibrant musical cadence, affectionate honorifics ('Basha'), and quick humor.",
            codeSwitchingDescription = "Natural mixing of colloquial Egyptian with English and French loanwords.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "قشطة (Eshta)",
                    meaning = "Awesome, sweet, agreed, deal! (literally: Fresh Cream)",
                    region = "Cairo, Egypt",
                    dialect = "Cairene Egyptian",
                    context = "Confirming agreement, excitement, or satisfaction",
                    formalEquivalent = "موافق / رائع (Muwafeq / Ra'e)",
                    exampleSentence = "نتقابل الساعة سبعة على القهوة؟ — قشطة!",
                    toneCategory = "Casual",
                    culturalNotes = "Originating from the richness of clotted cream, symbolizing high pleasure and smooth deals."
                ),
                RegionalExpression(
                    expression = "زي الفل (Zay El Fol)",
                    meaning = "Wonderful, tip-top, blooming great (literally: Like Arabian Jasmine)",
                    region = "Cairo, Egypt",
                    dialect = "Cairene Egyptian",
                    context = "Describing your mood, health, or status of a project",
                    formalEquivalent = "ممتاز / على أكمل وجه",
                    exampleSentence = "صحتك عاملة إيه يا حاج؟ — زي الفل الحمد لله.",
                    toneCategory = "Friendly",
                    culturalNotes = "Reflects the poetic botanical imagery deeply rooted in Egyptian folk speech."
                )
            )
        ),

        // 9. Andalusian Spanish
        RegionalDialect(
            id = "es_es_andalusia_seville",
            language = "Spanish",
            country = "Spain",
            region = "Andalusia",
            cityOrArea = "Seville",
            dialectName = "Andalusian Style",
            flagEmoji = "🇪🇸",
            samplePhrases = listOf(
                "¡Qué pasa quillo! ¿Cómo estás?",
                "Eso no ni ná, no te preocupes.",
                "Vamos a tomarnos una tapita al solecito.",
                "¡Miarma, qué arte más grande tienes!"
            ),
            greeting = "¡Hombre quillo! ¿Qué tal la cosa? ¡Qué alegría verte por aquí, miarma!",
            localeCode = "es-ES",
            description = "Famous for rhythmic s-dropping (ceceo/seseo), heartfelt warmth, dramatic expressive flair, and flamenco passion.",
            codeSwitchingDescription = "Vibrant colloquial Southern Spanish with classic Andalusian idioms.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Quillo / Illo",
                    meaning = "Buddy, pal, mate (short for chiquillo)",
                    region = "Andalusia, Spain",
                    dialect = "Andalusian Style",
                    context = "Calling out to someone or catching their attention warmly",
                    formalEquivalent = "Amigo / Compañero",
                    exampleSentence = "¿Qué pasa quillo, te vienes a la plaza o qué?",
                    toneCategory = "Casual",
                    culturalNotes = "The universal friendly shout across Cadiz, Seville, and Malaga streets."
                ),
                RegionalExpression(
                    expression = "No ni ná",
                    meaning = "Of course! Absolutely! Without a shadow of doubt! (literally: Not nor nothing)",
                    region = "Andalusia, Spain",
                    dialect = "Andalusian Style",
                    context = "Triple negative used emphatically to mean a resounding YES",
                    formalEquivalent = "Por supuesto / Claro que sí",
                    exampleSentence = "¿Vas a comerte ese plato de jamón? — ¡No ni ná!",
                    toneCategory = "Humorous",
                    culturalNotes = "One of Spanish linguistics' most celebrated triple negative colloquial gems."
                ),
                RegionalExpression(
                    expression = "Miarma",
                    meaning = "My soul, sweetheart, darling (short for Mi alma)",
                    region = "Seville, Andalusia",
                    dialect = "Andalusian Style",
                    context = "Endearing address between locals in shops, markets, and gatherings",
                    formalEquivalent = "Cariño / Alma mía",
                    exampleSentence = "Gracias miarma, que tengas un día estupendo.",
                    toneCategory = "Affectionate",
                    culturalNotes = "Emblem of Sevillian emotional generosity and poetic warmth."
                )
            )
        ),

        // 10. Mexican Chilango Spanish
        RegionalDialect(
            id = "es_mx_cdmx",
            language = "Spanish",
            country = "Mexico",
            region = "Mexico City",
            cityOrArea = "CDMX",
            dialectName = "Mexican Chilango Style",
            flagEmoji = "🇲🇽",
            samplePhrases = listOf(
                "¿Qué onda güey? ¿Cómo andas?",
                "Está bien chido este lugar, la neta.",
                "No manches, ¿a poco sí pasó eso?",
                "Sale y vale, nos vemos al rato."
            ),
            greeting = "¡Qué tranza, mi buen! ¿Cómo andas güey? Vamos a echarnos unos tacos y platicar.",
            localeCode = "es-MX",
            description = "Sing-song intonation, witty double-entendres (albur), ubiquitous 'güey' and 'chido', and warm neighborhood flavor.",
            codeSwitchingDescription = "Spanglish blend common in youth culture and modern Mexican media.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Chido",
                    meaning = "Cool, great, fantastic, awesome",
                    region = "Mexico City, Mexico",
                    dialect = "Mexican Chilango Style",
                    context = "Describing anything you really like",
                    formalEquivalent = "Excelente / Muy bueno",
                    exampleSentence = "¡Qué chida está tu chamarra nueva!",
                    toneCategory = "Casual",
                    culturalNotes = "Essential Mexico City slang popularized throughout Latin America."
                ),
                RegionalExpression(
                    expression = "No manches",
                    meaning = "No way! You're kidding! Don't mess with me!",
                    region = "Mexico City, Mexico",
                    dialect = "Mexican Chilango Style",
                    context = "Expressing surprise, shock, or humorous disbelief",
                    formalEquivalent = "No me digas / ¿En serio?",
                    exampleSentence = "¡No manches! ¿Ganaron el partido en el último minuto?",
                    toneCategory = "Humorous",
                    culturalNotes = "Polite colloquial variant of stronger street exclamations, safe in any conversation."
                )
            )
        ),

        // 11. Mumbai Bambaiya Hindi
        RegionalDialect(
            id = "hi_in_mh_mumbai",
            language = "Hindi",
            country = "India",
            region = "Maharashtra",
            cityOrArea = "Mumbai",
            dialectName = "Mumbai Bambaiya / Tapori",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "अपुन को सब मालूम है, लोड मत ले!",
                "क्या बोलती पब्लिक? सब बिंदास ना?",
                "एक नंबर वड़ा पाव मिला स्टेशन के बाहर",
                "खाली-पीली टाइम पास मत कर बंटाई!"
            ),
            greeting = "अरे क्या बोलते बंटाई! सब झकास? अपुन के साथ गपशप मारने का मूड है क्या?",
            localeCode = "hi-IN",
            description = "Legendary Mumbai street swagger mixing Hindi, Marathi, Gujarati, English, and Bollywood flair into high-energy banter.",
            codeSwitchingDescription = "Heavy, effortless Mumbai Hinglish blending English loan words with local street slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "अपुन (Apun)",
                    meaning = "I / Me / Myself (street first person)",
                    region = "Mumbai, Maharashtra",
                    dialect = "Mumbai Bambaiya / Tapori",
                    context = "Used instead of 'Main' to convey confident street swagger",
                    formalEquivalent = "मैं (Main)",
                    exampleSentence = "अपुन जो बोलता है वो करके दिखाता है!",
                    toneCategory = "Street Slang",
                    culturalNotes = "Immortalized in Bollywood cinema as the voice of Mumbai's resilient, streetwise character."
                ),
                RegionalExpression(
                    expression = "बंटाई (Bantai)",
                    meaning = "Bro, homie, close friend",
                    region = "Mumbai, Maharashtra",
                    dialect = "Mumbai Bambaiya / Tapori",
                    context = "Greeting your circle of friends in gully rap and street talk",
                    formalEquivalent = "दोस्त / भाई (Dost / Bhai)",
                    exampleSentence = "क्या चल रहा है बंटाई? आज शाम को चाय पे मिलते हैं.",
                    toneCategory = "Casual",
                    culturalNotes = "Rooted in Mumbai local street culture and popularized worldwide via Mumbai hip-hop."
                ),
                RegionalExpression(
                    expression = "झकास (Jhakas)",
                    meaning = "Superb, fantastic, first-class",
                    region = "Mumbai, Maharashtra",
                    dialect = "Mumbai Bambaiya / Tapori",
                    context = "Applauding high quality or wonderful news",
                    formalEquivalent = "बहुत बढ़िया (Bahut badhiya)",
                    exampleSentence = "तेरा नया आइडिया एकदम झकास है भाई!",
                    toneCategory = "Humorous",
                    culturalNotes = "The iconic exclamation made famous by Anil Kapoor and cherished across Maharashtra."
                )
            )
        ),

        // 12. Delhi NCR Hindi
        RegionalDialect(
            id = "hi_in_dl_delhi",
            language = "Hindi",
            country = "India",
            region = "Delhi",
            cityOrArea = "Delhi",
            dialectName = "Delhi NCR Slang",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "भाई क्या सीन है आज का?",
                "जुगाड़ कर लेंगे, तू टेंशन मत ले",
                "कसम से एकदम बवाल चीज़ है",
                "यार छोले भटूरे खाने सीपी चलें?"
            ),
            greeting = "हाँ भाई! क्या हाल चाल? क्या सीन बन रहा है आज का? बताओ फिर!",
            localeCode = "hi-IN",
            description = "Bold, loud, humorous North Indian conversational rhythm filled with 'Jugaad', 'Scene', and energetic fraternal charm.",
            codeSwitchingDescription = "Intense, stylish Delhi Hinglish widely spoken across colleges, startups, and CP streets.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "सीन क्या है (Scene kya hai)",
                    meaning = "What's the plan? What's going on?",
                    region = "Delhi NCR",
                    dialect = "Delhi NCR Slang",
                    context = "Asking what everyone is planning to do for fun, food, or hangout",
                    formalEquivalent = "क्या योजना है? (Kya yojana hai?)",
                    exampleSentence = "अरे भाई वीकेंड का क्या सीन है? कहीं बाहर चलें?",
                    toneCategory = "Casual",
                    culturalNotes = "Every Delhi youth hangout begins with this exact five-letter inquiry."
                ),
                RegionalExpression(
                    expression = "जुगाड़ (Jugaad)",
                    meaning = "Ingenious hack, smart workaround, resourceful fix",
                    region = "Delhi / Northern India",
                    dialect = "Delhi NCR Slang",
                    context = "Solving any tough problem through local creative resourcefulness",
                    formalEquivalent = "समाधान / तरकीब (Tarkeeb)",
                    exampleSentence = "गाड़ी पंचर हो गई थी लेकिन भाई ने गजब का जुगाड़ लगा दिया!",
                    toneCategory = "Casual",
                    culturalNotes = "A world-renowned philosophy of frugal, agile Indian innovation."
                )
            )
        )
    )

    fun getDialectById(id: String): RegionalDialect {
        return dialects.find { it.id == id } ?: dialects.first()
    }

    fun getAllLanguages(): List<String> = dialects.map { it.language }.distinct()

    fun getCountriesForLanguage(language: String): List<String> =
        dialects.filter { it.language.equals(language, ignoreCase = true) }
            .map { it.country }.distinct()

    fun getRegionsForCountry(country: String): List<String> =
        dialects.filter { it.country.equals(country, ignoreCase = true) }
            .map { it.region }.distinct()

    fun getCitiesForRegion(region: String): List<String> =
        dialects.filter { it.region.equals(region, ignoreCase = true) }
            .map { it.cityOrArea }.distinct()

    fun getDialectsForCity(city: String): List<RegionalDialect> =
        dialects.filter { it.cityOrArea.equals(city, ignoreCase = true) }

    fun searchDialects(query: String): List<RegionalDialect> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return dialects
        return dialects.filter {
            it.dialectName.lowercase().contains(q) ||
            it.language.lowercase().contains(q) ||
            it.cityOrArea.lowercase().contains(q) ||
            it.region.lowercase().contains(q) ||
            it.country.lowercase().contains(q) ||
            it.typicalExpressions.any { expr -> expr.expression.lowercase().contains(q) || expr.meaning.lowercase().contains(q) }
        }
    }
}
