package com.example.data.repository

import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression

object InternationalDialects {

    val allInternationalDialects: List<RegionalDialect> = listOf(
        // 1. Liverpool Scouse English
        RegionalDialect(
            id = "en_gb_eng_liverpool",
            language = "English",
            country = "United Kingdom",
            stateOrProvince = "England",
            region = "Merseyside",
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
            tendenciesNotes = "Known for distinctive intonation, adenoidal vocal placement, and affectionate Scouse slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Sound",
                    meaning = "Good, reliable, decent, cool, or genuine",
                    englishMeaning = "Good, reliable, genuine",
                    region = "Merseyside",
                    district = "Liverpool",
                    dialect = "Liverpool Scouse",
                    context = "Used to describe a great person, confirm agreement, or ask how someone is doing",
                    formalEquivalent = "Good / Reliable (Decent)",
                    casualEquivalent = "Cool / Alright",
                    exampleSentence = "Don't worry about Jack, he's sound as a pound mate.",
                    category = "Everyday conversation",
                    toneCategory = "Friendly",
                    culturalNotes = "Ubiquitous affirmative in Liverpool, signalling trustworthiness and easygoing friendship.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "Boss",
                    meaning = "Excellent, great, outstanding, top-tier",
                    englishMeaning = "Excellent, great, fantastic",
                    region = "Merseyside",
                    district = "Liverpool",
                    dialect = "Liverpool Scouse",
                    context = "Praising music, food, experiences, or a person's idea",
                    formalEquivalent = "Excellent / Wonderful",
                    casualEquivalent = "Great",
                    exampleSentence = "That scran was proper boss, best curry in town!",
                    category = "Happiness",
                    toneCategory = "Enthusiastic",
                    culturalNotes = "Liverpool's supreme adjective of unreserved high approval.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 2. London Cockney / Estuary English
        RegionalDialect(
            id = "en_gb_eng_london",
            language = "English",
            country = "United Kingdom",
            stateOrProvince = "England",
            region = "Greater London",
            cityOrArea = "London",
            dialectName = "London Style",
            flagEmoji = "🇬🇧",
            samplePhrases = listOf(
                "Alright mate, how's it going?",
                "Fancy a cuppa tea, bruv?",
                "That's proper decent innit?",
                "Catch you round the manor later."
            ),
            greeting = "Alright mate! How's tricks? Good to see ya, let's catch up.",
            localeCode = "en-GB",
            description = "Iconic London cadence featuring glottal T-drops, rhyming slang heritage, and multicultural Estuary rhythm.",
            codeSwitchingDescription = "Mixes classic Cockney with modern multicultural London English (MLE).",
            tendenciesNotes = "Vibrant melting pot blending East End history with energetic urban British rhythm.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Proper (Proper good / Proper decent)",
                    meaning = "Really, truly, genuinely",
                    englishMeaning = "Really, truly, totally",
                    region = "Greater London",
                    district = "London",
                    dialect = "London Style",
                    context = "Emphasizing quality or intensity in everyday speech",
                    formalEquivalent = "Very / Genuinely",
                    casualEquivalent = "Really",
                    exampleSentence = "Mate, that roast dinner was proper delicious.",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Key London intensifier used across multiple generations.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "Innit (Isn't it)",
                    meaning = "Right? / Don't you agree?",
                    englishMeaning = "Right? / Isn't that so?",
                    region = "Greater London",
                    district = "London",
                    dialect = "London Style",
                    context = "Tag question confirming consensus at end of sentences",
                    formalEquivalent = "Isn't it so? / Don't you agree?",
                    casualEquivalent = "Right?",
                    exampleSentence = "It's freezing outside today, innit?",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Classic British confirmation tag.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 3. Scotland Glaswegian / Scots English
        RegionalDialect(
            id = "en_gb_sct_glasgow",
            language = "English",
            country = "United Kingdom",
            stateOrProvince = "Scotland",
            region = "Lowlands",
            cityOrArea = "Glasgow",
            dialectName = "Scottish Style",
            flagEmoji = "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
            samplePhrases = listOf(
                "How's it gaun, pal?",
                "That's pure dead brilliant!",
                "Dae ye fancy a wee cuppa?",
                "Nae worries at all, big man."
            ),
            greeting = "Awrite pal! How's it gaun? Grand to see ye, grab a seat!",
            localeCode = "en-GB",
            description = "Rich, rhythmic Scottish cadence with rhotic R's, warm colloquialisms like 'wee', 'bonnie', and 'pure dead brilliant'.",
            codeSwitchingDescription = "Standard English interwoven with classic Scottish vocabulary.",
            tendenciesNotes = "Celebrated for high warmth, direct honesty, and lyrical musicality.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Wee",
                    meaning = "Small, little, quick, endearing",
                    englishMeaning = "Small / A little bit / Endearing",
                    region = "Scotland",
                    district = "Glasgow",
                    dialect = "Scottish Style",
                    context = "Universally attached to food, time, drinks, or small gestures",
                    formalEquivalent = "Small / Brief",
                    casualEquivalent = "Little",
                    exampleSentence = "Let's take a wee break and have a cup of tea.",
                    category = "Everyday conversation",
                    toneCategory = "Warm",
                    culturalNotes = "The most beloved word in Scottish daily life.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 4. Ireland Dublin English
        RegionalDialect(
            id = "en_ie_leinster_dublin",
            language = "English",
            country = "Ireland",
            stateOrProvince = "Leinster",
            region = "Dublin",
            cityOrArea = "Dublin",
            dialectName = "Dublin Style",
            flagEmoji = "🇮🇪",
            samplePhrases = listOf(
                "What's the story, bud?",
                "We had mighty craic last night!",
                "Grand so, I'll see you later.",
                "That was deadly, fair play!"
            ),
            greeting = "What's the story! How're ya keepin'? Mighty to chat with you.",
            localeCode = "en-IE",
            description = "Lyrical Irish lilt famous for storytelling wit, 'the craic', and effortless social warmth.",
            codeSwitchingDescription = "Hiberno-English idioms mixed with modern European cosmopolitan speech.",
            tendenciesNotes = "Signature words include 'craic' (fun/banter) and 'grand' (satisfactory).",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Craic (The Craic)",
                    meaning = "Fun, news, entertainment, banter, good times",
                    englishMeaning = "Fun, good times, entertainment, news",
                    region = "Leinster",
                    district = "Dublin",
                    dialect = "Dublin Style",
                    context = "Asking for news or describing a fun social gathering",
                    formalEquivalent = "Enjoyment / News / Social entertainment",
                    casualEquivalent = "Good time",
                    exampleSentence = "How was the festival? — Ah, the craic was ninety!",
                    category = "Everyday conversation",
                    toneCategory = "Friendly",
                    culturalNotes = "The central cultural concept of Irish social warmth.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 5. Australia Melbourne / Sydney English
        RegionalDialect(
            id = "en_au_nsw_sydney",
            language = "English",
            country = "Australia",
            stateOrProvince = "New South Wales",
            region = "East Coast",
            cityOrArea = "Sydney",
            dialectName = "Aussie Style",
            flagEmoji = "🇦🇺",
            samplePhrases = listOf(
                "G'day mate, how's it going?",
                "No worries at all, all good!",
                "Catch you this arvo at the beach.",
                "That's heaps good, legend!"
            ),
            greeting = "G'day mate! How're ya going? Beautiful day for a chat, no worries!",
            localeCode = "en-AU",
            description = "Relaxed, sun-drenched Australian cadence with signature rising intonation and diminutive word endings.",
            codeSwitchingDescription = "Effortless Aussie slang and friendly banter.",
            tendenciesNotes = "Known for shortenings like 'arvo' (afternoon) and universal 'no worries'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "No worries",
                    meaning = "You're welcome / Not a problem / Everything is fine",
                    englishMeaning = "No problem / You're welcome / It's all good",
                    region = "East Coast",
                    district = "Sydney",
                    dialect = "Aussie Style",
                    context = "Default polite response to thanks, apologies, or requests",
                    formalEquivalent = "You are very welcome / Please do not worry",
                    casualEquivalent = "All good",
                    exampleSentence = "Thanks for picking up the coffee! — No worries mate!",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Defines the relaxed, unflappable Australian worldview.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 6. United States New York Brooklyn English
        RegionalDialect(
            id = "en_us_ny_brooklyn",
            language = "English",
            country = "United States",
            stateOrProvince = "New York",
            region = "New York City",
            cityOrArea = "Brooklyn",
            dialectName = "Brooklyn Style",
            flagEmoji = "🇺🇸",
            samplePhrases = listOf(
                "Yo, what's good with you?",
                "Fuhgeddaboudit, don't sweat it.",
                "Best pizza in the five boroughs, deadass.",
                "Talk to me, how you doing?"
            ),
            greeting = "Yo, what's good! How you doing? Welcome to New York, let's talk!",
            localeCode = "en-US",
            description = "Fast-paced, vibrant New York City rhythm with authentic Brooklyn swagger, non-rhotic vowel shifts, and direct warmth.",
            codeSwitchingDescription = "Iconic NYC street idioms blended with classic neighborhood warmth.",
            tendenciesNotes = "Famous for direct, honest communication and signature NYC phrases like 'fuhgeddaboudit' and 'deadass'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Fuhgeddaboudit",
                    meaning = "Forget about it / No problem / Don't worry about it / Absolutely",
                    englishMeaning = "Forget about it / No doubt",
                    region = "New York City",
                    district = "Brooklyn",
                    dialect = "Brooklyn Style",
                    context = "Used to dismiss a problem or conversely emphasize absolute certainty",
                    formalEquivalent = "Do not worry / Certainly",
                    casualEquivalent = "No problem",
                    exampleSentence = "Can you make it tonight? — Fuhgeddaboudit, I'm already there!",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Quintessential Brooklyn badge of certainty and easygoing camaraderie.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "Deadass",
                    meaning = "Seriously / In all honesty / Completely truthful",
                    englishMeaning = "For real / Seriously",
                    region = "New York City",
                    district = "Brooklyn",
                    dialect = "Brooklyn Style",
                    context = "Confirming that what you are saying is completely genuine without exaggeration",
                    formalEquivalent = "Honestly / Seriously speaking",
                    casualEquivalent = "For real",
                    exampleSentence = "That was deadass the best slice I've ever had.",
                    category = "Everyday conversation",
                    toneCategory = "Direct",
                    culturalNotes = "Modern New York slang emphasizing raw truthfulness.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 6. Kuwaiti Arabic
        RegionalDialect(
            id = "ar_kw_kuwait",
            language = "Arabic",
            country = "Kuwait",
            stateOrProvince = "Al Asimah",
            region = "Kuwait City",
            cityOrArea = "Kuwait City",
            dialectName = "Kuwaiti Arabic",
            flagEmoji = "🇰🇼",
            samplePhrases = listOf(
                "شلونك يا الغالي؟ عساك بخير؟",
                "حياك الله في الدوانية",
                "والله خوش فكرة، تسلم",
                "ما قصرت يا الحبيب"
            ),
            greeting = "يا هلا ومسهلا! حياك الله يا الغالي! شلونك وشلون الأهل؟ عساكم طيبين وبخير؟",
            localeCode = "ar-KW",
            description = "Rich Arabian Gulf cadence blending classic Bedouin traditions, maritime history, and Diwaniya hospitality.",
            codeSwitchingDescription = "Mixes Khaleeji Arabic with English in tech, commerce, and everyday discussions.",
            tendenciesNotes = "Famous for expressions of generous hospitality like 'خوش' and 'ما قصرت'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "خوش (Khoosh)",
                    meaning = "Great, wonderful, fine, commendable",
                    englishMeaning = "Good, fine, wonderful, commendable",
                    region = "Kuwait City",
                    district = "Capital",
                    dialect = "Kuwaiti Arabic",
                    context = "Complimenting an idea, person, dish, or decision",
                    formalEquivalent = "جيد جداً / ممتاز",
                    casualEquivalent = "حلو",
                    exampleSentence = "هذا خوش مطعم، لازم تجربه معانا.",
                    category = "Everyday conversation",
                    toneCategory = "Warm",
                    culturalNotes = "A beloved Gulf expression rooted in historical regional trade.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "ما قصرت (Ma Gassart)",
                    meaning = "You haven't fallen short / Thank you deeply for your immense help",
                    englishMeaning = "Thank you so much / You went above and beyond",
                    region = "Kuwait City",
                    district = "Capital",
                    dialect = "Kuwaiti Arabic",
                    context = "Heartfelt Gulf gratitude when someone does a favor or extends hospitality",
                    formalEquivalent = "شكراً جزيلاً لك على كرمك",
                    casualEquivalent = "مشكور",
                    exampleSentence = "بيض الله وجهك يا خوي، والله ما قصرت.",
                    category = "Friendship",
                    toneCategory = "Polite",
                    culturalNotes = "The pinnacle of Khaleeji social etiquette and gratitude.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 7. UAE Emirati Arabic
        RegionalDialect(
            id = "ar_ae_dubai",
            language = "Arabic",
            country = "United Arab Emirates",
            stateOrProvince = "Dubai Emirate",
            region = "Dubai",
            cityOrArea = "Dubai",
            dialectName = "Emirati Arabic",
            flagEmoji = "🇦🇪",
            samplePhrases = listOf(
                "شحالك يا خوي؟ عساك طيب؟",
                "يا مرحبا الساع، نورتنا",
                "فالك طيب وما يصير خاطرك إلا طيب",
                "طرش لي اللوكيشن على طول"
            ),
            greeting = "مرحبا الساع! حي الله من جانا! شحالك يا خوي؟ عساك مرتاح وبخير؟",
            localeCode = "ar-AE",
            description = "Gentle Khaleeji coastal dialect famous for welcoming greetings like 'Marhaba Al Saa' and international cosmopolitan flair.",
            codeSwitchingDescription = "Effortless Gulf Arabic blended with English in commerce and media.",
            tendenciesNotes = "Warm Emirati hospitality markers including 'فالك طيب' and 'مرحبا الساع'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "مرحبا الساع (Marhaba Al Saa)",
                    meaning = "Warmest welcome! You are welcome at this blessed hour",
                    englishMeaning = "A most warm and timely welcome",
                    region = "Dubai",
                    district = "Dubai",
                    dialect = "Emirati Arabic",
                    context = "Traditional Emirati greeting when guests arrive or friends meet",
                    formalEquivalent = "أهلاً وسهلاً بك في أي وقت",
                    casualEquivalent = "هلا والله",
                    exampleSentence = "مرحبا الساع يا بو خالد، نورت دارك!",
                    category = "Greetings",
                    toneCategory = "Warm",
                    culturalNotes = "Iconic Emirati welcome symbolizing generosity and honor.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 8. Saudi Arabian Hijazi / Najdi
        RegionalDialect(
            id = "ar_sa_riyadh",
            language = "Arabic",
            country = "Saudi Arabia",
            stateOrProvince = "Riyadh Province",
            region = "Najd",
            cityOrArea = "Riyadh",
            dialectName = "Saudi Style",
            flagEmoji = "🇸🇦",
            samplePhrases = listOf(
                "وش اخبارك يا غالي؟",
                "أبشر بالسعد، على خشمي",
                "الله يعطيك العافية ويبارك فيك",
                "يا هلا والله وغلا"
            ),
            greeting = "يا هلا والله ومسهلا! وش أخبارك يا الغالي؟ عساك طيب وبأحسن حال؟",
            localeCode = "ar-SA",
            description = "Respected Arabian heartland dialect renowned for honorifics, generosity idioms like 'Abshir', and deep linguistic lineage.",
            codeSwitchingDescription = "Traditional Najdi/Hijazi idioms paired with modern Saudi terminology.",
            tendenciesNotes = "Signature commitment phrase 'أبشر' (with pleasure / it will be done).",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "أبشر (Abshir)",
                    meaning = "Consider it done! / Gladly and with honor!",
                    englishMeaning = "With pleasure / Consider it done immediately",
                    region = "Najd",
                    district = "Riyadh",
                    dialect = "Saudi Style",
                    context = "Enthusiastic and respectful agreement to fulfill a request",
                    formalEquivalent = "بكل سرور وامتنان",
                    casualEquivalent = "حاضر",
                    exampleSentence = "تقدر تساعدني بهالمشروع؟ — أبشر بالسعد يا خوي!",
                    category = "Everyday conversation",
                    toneCategory = "Polite",
                    culturalNotes = "Represents Arabian commitment to service and brotherhood.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 9. Egyptian Cairene Arabic
        RegionalDialect(
            id = "ar_eg_cairo",
            language = "Arabic",
            country = "Egypt",
            stateOrProvince = "Cairo Governorate",
            region = "Cairo",
            cityOrArea = "Cairo",
            dialectName = "Egyptian Style",
            flagEmoji = "🇪🇬",
            samplePhrases = listOf(
                "عامل إيه يا باشا؟ كله تمام؟",
                "الموضوع ده زي الفل ومفيهوش مشاكل",
                "قشطة عليك يا فنان، نتقابل بكرة",
                "أنا بحبك أوي يا صاحبي"
            ),
            greeting = "يا مساء الفل! إزيك يا باشا؟ عامل إيه في يومك؟ كله تمام وزي الفل؟",
            localeCode = "ar-EG",
            description = "The universally understood dialect of Egyptian cinema, vibrant musical cadence, affectionate honorifics ('Basha'), and quick humor.",
            codeSwitchingDescription = "Natural mixing of colloquial Egyptian with English and French loanwords.",
            tendenciesNotes = "Beloved across the Arab world through cinema and humor.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "زي الفل (Zay El Fol)",
                    meaning = "Wonderful, tip-top, blooming great (literally: Like Arabian Jasmine)",
                    englishMeaning = "Wonderful, great, in blooming health",
                    region = "Cairo",
                    district = "Cairo",
                    dialect = "Egyptian Style",
                    context = "Describing your mood, health, or status of a project",
                    formalEquivalent = "ممتاز / على أكمل وجه",
                    casualEquivalent = "تمام",
                    exampleSentence = "صحتك عاملة إيه يا حاج؟ — زي الفل الحمد لله.",
                    category = "Happiness",
                    toneCategory = "Friendly",
                    culturalNotes = "Reflects the poetic botanical imagery deeply rooted in Egyptian folk speech.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 10. Spanish Andalusia Seville
        RegionalDialect(
            id = "es_es_andalusia_seville",
            language = "Spanish",
            country = "Spain",
            stateOrProvince = "Andalusia",
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
            description = "Famous for rhythmic s-dropping, heartfelt warmth, dramatic expressive flair, and flamenco passion.",
            codeSwitchingDescription = "Vibrant colloquial Southern Spanish with classic Andalusian idioms.",
            tendenciesNotes = "Rich in affectionate address terms like 'Quillo' and joyful social banter.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "Quillo / Illo",
                    meaning = "Buddy, pal, mate (short for chiquillo)",
                    englishMeaning = "Buddy, pal, mate",
                    region = "Andalusia",
                    district = "Seville",
                    dialect = "Andalusian Style",
                    context = "Calling out to someone or catching their attention warmly",
                    formalEquivalent = "Amigo / Compañero",
                    casualEquivalent = "Tío",
                    exampleSentence = "¡Quillo, ven pa'cá que te cuente una cosa buena!",
                    category = "Friendship",
                    toneCategory = "Friendly",
                    culturalNotes = "The universal friendly call across Seville and Cádiz.",
                    verificationStatus = "Verified"
                )
            )
        )
    )
}
