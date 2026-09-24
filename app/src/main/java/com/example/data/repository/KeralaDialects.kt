package com.example.data.repository

import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression

object KeralaDialects {

    val allKeralaDistricts: List<RegionalDialect> = listOf(
        // 1. Kasaragod
        RegionalDialect(
            id = "ml_in_kl_kasaragod",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "North Malabar",
            cityOrArea = "Kasaragod",
            dialectName = "Kasaragod Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "ബേജാറാവണ്ട, കാര്യം എളുപ്പം തീർക്കാം",
                "പോയേരെ, നമ്മക്ക് ഒരുമിച്ച് പോകാം",
                "നല്ല മൊഞ്ചുള്ള സ്ഥലമാണ് ഇത്",
                "എന്താണ്ട് വിശേഷങ്ങൾ ചങ്ങാതീ?"
            ),
            greeting = "നമസ്കാരം! കാസർഗോഡേക്ക് സ്വാഗതം! എന്തൊക്കെയുണ്ട് വിശേഷങ്ങൾ? സുഖല്ലേ?",
            localeCode = "ml-IN",
            description = "Northernmost Kerala vernacular influenced by historical proximity to Tulu, Beary, and Kannada coastal traditions.",
            codeSwitchingDescription = "Features unique northern border vocabulary alongside English terms.",
            tendenciesNotes = "Regional tendencies vary by coastal belts and interior taluks. Words like 'ബേജാറാവണ്ട' (don't worry) and 'പോയേരെ' (let's go) reflect regional warmth.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ബേജാറാവണ്ട (Bejaravanda)",
                    meaning = "വിഷമിക്കേണ്ട, പേടിക്കേണ്ട",
                    englishMeaning = "Do not worry / Don't be anxious",
                    region = "Kerala",
                    district = "Kasaragod",
                    dialect = "Kasaragod Style",
                    context = "Consoling or reassuring someone facing a difficulty",
                    formalEquivalent = "വിഷമിക്കേണ്ടതില്ല (Vishamikkendathilla)",
                    casualEquivalent = "ടെൻഷൻ അടിക്കല്ലേ",
                    exampleSentence = "ബേജാറാവണ്ട കൂട്ടുകാരാ, എല്ലാം നന്നാവും.",
                    category = "Everyday conversation",
                    toneCategory = "Warm",
                    culturalNotes = "Arabic-derived term naturalized across Northern Malabar and border communities.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("സാരമില്ല", "പേടിക്കണ്ട")
                ),
                RegionalExpression(
                    expression = "പോയേരെ (Poyere)",
                    meaning = "നമുക്ക് പോകാം / വരൂ",
                    englishMeaning = "Let us go together / Come along",
                    region = "Kerala",
                    district = "Kasaragod",
                    dialect = "Kasaragod Style",
                    context = "Inviting friends to set out or head to a spot",
                    formalEquivalent = "നമുക്ക് പുറപ്പെടാം (Namukku purappedam)",
                    casualEquivalent = "വാ പോകാം",
                    exampleSentence = "നേരം വൈകി, പോയേരെ ബീച്ചിലോട്ട്!",
                    category = "Travel",
                    toneCategory = "Casual",
                    culturalNotes = "Characteristic inviting imperative heard across town gatherings.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("വാ പോകാം")
                )
            )
        ),

        // 2. Kannur
        RegionalDialect(
            id = "ml_in_kl_kannur",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "North Malabar",
            cityOrArea = "Kannur",
            dialectName = "Kannur Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "കൂയ്, എന്താണ്ട് വിശേഷം?",
                "ഓര് ഇന്നലെ നാട്ടിൽ വന്നല്ലോ",
                "നമ്മക്ക് ചായ കുടിച്ചൂടെ?",
                "തീപ്പൊരി പരിപാടിയാണ് ട്ടോ"
            ),
            greeting = "കൂയ് ചങ്ങായി! കണ്ണൂരിലേക്ക് സ്വാഗതം! എന്തുണ്ട് വിശേഷങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Distinctive North Malabar intonation with signature vocatives like 'Kooy', phonetic shifts, and hearty warmth.",
            codeSwitchingDescription = "Mixes northern vernacular with trade terms and English.",
            tendenciesNotes = "Famous for 'കൂയ്' (hey there) and respectful third-person 'ഓര്'. intonations vary between coastal Payyannur and Thalassery.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "കൂയ് (Kooy)",
                    meaning = "ഹലോ / ഇവിടെ നോക്കൂ",
                    englishMeaning = "Hey there! / Hello! (affectionate call)",
                    region = "Kerala",
                    district = "Kannur",
                    dialect = "Kannur Style",
                    context = "Calling out to close friends or family affectionately",
                    formalEquivalent = "ശ്രദ്ധിക്കൂ / ഹലോ",
                    casualEquivalent = "ഹേയ്",
                    exampleSentence = "കൂയ്, എവിടെയാ ഉള്ളത്? ഇങ്ങോട്ട് വന്നേ!",
                    category = "Greetings",
                    toneCategory = "Casual",
                    culturalNotes = "The famous friendly shout of Northern Kerala coastlines.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("ഡാ", "ചെങ്ങാതീ")
                ),
                RegionalExpression(
                    expression = "ഓര് (Oru)",
                    meaning = "അദ്ദേഹം / അവർ",
                    englishMeaning = "He / She / They (respectful colloquial)",
                    region = "Kerala",
                    district = "Kannur",
                    dialect = "Kannur Style",
                    context = "Referring politely to someone known",
                    formalEquivalent = "അദ്ദേഹം / അവർ",
                    casualEquivalent = "അവൻ / അവൾ",
                    exampleSentence = "ഓര് ഇന്നലെ പയ്യാമ്പലത്ത് ഉണ്ടായിരുന്നു.",
                    category = "Everyday conversation",
                    toneCategory = "Friendly",
                    culturalNotes = "Signature North Malabar grammatical feature blending respect and colloquial comfort.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 3. Wayanad
        RegionalDialect(
            id = "ml_in_kl_wayanad",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Highland Malabar",
            cityOrArea = "Wayanad",
            dialectName = "Wayanad Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "നല്ല കുളിരുള്ള കാലാവസ്ഥയാണ്",
                "തോട്ടത്തിൽ പണി തകൃതിയായി നടക്കുന്നു",
                "ചുരം ഇറങ്ങി വരാൻ സമയം എടുക്കും",
                "ഇവിടെ നല്ല സമാധാനമാണ്"
            ),
            greeting = "സ്വാഗതം! വയനാടൻ കുളിരിൽ നിന്ന് സ്നേഹാന്വേഷണങ്ങൾ! വിശേഷങ്ങൾ പറയൂ.",
            localeCode = "ml-IN",
            description = "Highland plantation speech blending settlers' dialects from central Kerala with indigenous regional vernacular.",
            codeSwitchingDescription = "Agricultural & plantation vocabulary mixed with modern Malayalam.",
            tendenciesNotes = "Wayanad's speech reflects its settler mosaic—blends of Travancore and Malabar idioms among lush tea and coffee hills.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ചുരം (Churam)",
                    meaning = "ഘട്ട് റോഡ് / മലമ്പാത",
                    englishMeaning = "Mountain pass / Ghat road",
                    region = "Kerala",
                    district = "Wayanad",
                    dialect = "Wayanad Style",
                    context = "Central landmark of Wayanad connectivity and life",
                    formalEquivalent = "മലമ്പാത (Ghat road)",
                    casualEquivalent = "റോഡ്",
                    exampleSentence = "ചുരത്തിൽ ഇന്ന് നല്ല കോടമഞ്ഞാണ്.",
                    category = "Travel",
                    toneCategory = "Everyday",
                    culturalNotes = "Thamarassery Churam is the cultural gateway connecting Wayanad to the coast.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 4. Kozhikode
        RegionalDialect(
            id = "ml_in_kl_kozhikode",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Malabar",
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
            tendenciesNotes = "Celebrated worldwide for genuine warmth, hospitality, and cinema-famous slang terms.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ചങ്ങായി (Changayi)",
                    meaning = "പ്രിയ സുഹൃത്ത് / കൂട്ടുകാരൻ",
                    englishMeaning = "Dear close friend, companion or pal",
                    region = "Kerala",
                    district = "Kozhikode",
                    dialect = "Kozhikode Slang",
                    context = "Used affectionately when greeting or referring to friends in everyday talks",
                    formalEquivalent = "സുഹൃത്ത് (Suhrith)",
                    casualEquivalent = "കൂട്ടുകാരൻ",
                    exampleSentence = "നമ്മുടെ ചങ്ങായി വന്നിട്ടുണ്ട്, ഒരു സുലൈമാനി കൊടുക്ക്!",
                    category = "Friendship",
                    toneCategory = "Friendly",
                    culturalNotes = "Quintessential North Malabar warmth symbolizing brotherhood and deep community hospitality.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("അളിയൻ", "ഭായ്", "ബ്രോ")
                ),
                RegionalExpression(
                    expression = "സുലൈമാനി (Sulaimani)",
                    meaning = "ഏലക്കയും നാരങ്ങാനീരും ചേർത്ത കട്ടൻചായ",
                    englishMeaning = "Spiced black tea with cardamom and lemon",
                    region = "Kerala",
                    district = "Kozhikode",
                    dialect = "Kozhikode Slang",
                    context = "The centerpiece of Kozhikode conversations, bonding, and deep discussions",
                    formalEquivalent = "കട്ടൻ ചായ (Black tea)",
                    casualEquivalent = "ചായ",
                    exampleSentence = "ചൂട് ബിരിയാണിക്ക് ശേഷം ഒരു സുലൈമാനി നിർബന്ധമാണ്.",
                    category = "Food",
                    toneCategory = "Warm",
                    culturalNotes = "Celebrated across Kozhikode culture and movies as the spirit of heartfelt hospitality.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "ഖൽബ് (Qalb)",
                    meaning = "ഹൃദയം / മനസ്സ്",
                    englishMeaning = "Heart, soul, affection",
                    region = "Kerala",
                    district = "Kozhikode",
                    dialect = "Kozhikode Slang",
                    context = "Expressing deep affection or genuine feeling",
                    formalEquivalent = "ഹൃദയം (Hrudayam)",
                    casualEquivalent = "മനസ്സ്",
                    exampleSentence = "അവന്റെ പെരുമാറ്റം ഖൽബിൽ തട്ടിപ്പോയി.",
                    category = "Love",
                    toneCategory = "Affectionate",
                    culturalNotes = "Deeply rooted in Mappila cultural heritage and poetry.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 5. Malappuram
        RegionalDialect(
            id = "ml_in_kl_malappuram",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Ernad & Valluvanad",
            cityOrArea = "Malappuram",
            dialectName = "Malappuram Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "ഇജ്ജ് എപ്പോ എത്തി ചങ്ങാതീ?",
                "ന്റ റബ്ബേ! അത് വല്ലാത്തൊരു സീനായിപ്പോയി",
                "നമ്മള് കട്ടക്ക് കൂടെയുണ്ടാവും ട്ടോ",
                "എന്ത്യേ ഇപ്പൊ അങ്ങനെയൊക്കെ ചോദിക്കാൻ?"
            ),
            greeting = "അസ്സലാമു അലൈക്കും ചങ്ങാതീ! എന്തൊക്കെയുണ്ട് മലപ്പുറത്ത് നിന്നുള്ള വിശേഷങ്ങൾ? സുഖമല്ലേ?",
            localeCode = "ml-IN",
            description = "Rhythmic, emotive Ernad/Valluvanad cadence characterized by unique pronouns, friendly emphasis, and soccer passion.",
            codeSwitchingDescription = "Infuses traditional heritage expressions alongside modern Manglish phrases.",
            tendenciesNotes = "Known for pronouns like 'ഇജ്ജ്' (you) and heartfelt interjections like 'ന്റെ റബ്ബേ!'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ഇജ്ജ് (Ijj)",
                    meaning = "നീ / താങ്കൾ",
                    englishMeaning = "You (casual, intimate pronoun)",
                    region = "Kerala",
                    district = "Malappuram",
                    dialect = "Malappuram Style",
                    context = "Direct intimate address to close friends or family",
                    formalEquivalent = "നീ / താങ്കൾ",
                    casualEquivalent = "നീ",
                    exampleSentence = "ഇജ്ജ് ഇന്ന് വൈകുന്നേരം സെവൻസ് ഫുട്ബോൾ കാണാൻ വരുന്നോ?",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Classical Ernad linguistic marker distinguishing casual intimate banter.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "ന്റെ റബ്ബേ (Nte Rabbe)",
                    meaning = "ദൈവമേ! / അത്ഭുതപ്രകടനം",
                    englishMeaning = "My goodness! / Oh my Lord! (astonishment)",
                    region = "Kerala",
                    district = "Malappuram",
                    dialect = "Malappuram Style",
                    context = "Expressing astonishment, relief, or intense reaction",
                    formalEquivalent = "ദൈവമേ (Oh God)",
                    casualEquivalent = "എന്റെ ദൈവമേ",
                    exampleSentence = "ന്റെ റബ്ബേ! ആ ഗോൾ കണ്ടോ ഇജ്ജ്?",
                    category = "Surprise",
                    toneCategory = "Emphatic",
                    culturalNotes = "Ubiquitous expression of emotion reflecting deep-seated regional roots.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "കട്ടക്ക് (Kattakku)",
                    meaning = "പൂർണ്ണമായി, ഉറച്ചു കൂടെ നിൽക്കുക",
                    englishMeaning = "Firmly together / Unconditionally supportive",
                    region = "Kerala",
                    district = "Malappuram",
                    dialect = "Malappuram Style",
                    context = "Pledging loyalty or solid backup to a friend",
                    formalEquivalent = "ദൃഢമായി ഒപ്പം (Together strongly)",
                    casualEquivalent = "കൂടെയുണ്ട്",
                    exampleSentence = "നീ പേടിക്കണ്ട, നമ്മൾ കട്ടക്ക് കൂടെയുണ്ട്!",
                    category = "Friendship",
                    toneCategory = "Supportive",
                    culturalNotes = "Widely popularized through local sports clubs and community solidarity.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 6. Palakkad
        RegionalDialect(
            id = "ml_in_kl_palakkad",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Valluvanad & Border",
            cityOrArea = "Palakkad",
            dialectName = "Palakkad Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "എന്തര് ഇപ്പൊ അങ്ങനെ പറഞ്ഞത്?",
                "നമ്മുടെ പാടത്ത് കൊയ്ത്തു തുടങ്ങി",
                "അങ്ങനെയാ കാര്യങ്ങൾ നടക്കേണ്ടത്",
                "ചൂട് ഇത്തിരി കൂടുതലാണ് ഇത്തവണ"
            ),
            greeting = "നമസ്കാരം! പാലക്കാടൻ കോട്ടയുടെയും കൽപ്പാത്തിയുടെയും മണ്ണിൽ നിന്ന് സ്വാഗതം! എന്തുണ്ട് വിശേഷങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Soft, agrarian Valluvanad Malayalam blended gracefully with Tamil phonetic nuances along the Palakkad Gap.",
            codeSwitchingDescription = "Agricultural terms and Tamil-influenced phonetic endings.",
            tendenciesNotes = "Shows phonetic shifts like 'എന്തര്' (what) and gentle prolonged vowel endings from border harmony.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "എന്തര് (Entharu)",
                    meaning = "എന്താണ് / എന്താ വിശേഷം",
                    englishMeaning = "What is it? / What happened?",
                    region = "Kerala",
                    district = "Palakkad",
                    dialect = "Palakkad Style",
                    context = "Inquiring about someone's opinion or an event",
                    formalEquivalent = "എന്താണ് (What is it)",
                    casualEquivalent = "എന്താ",
                    exampleSentence = "എന്തര്, ഇന്നെന്താ ഇത്ര വൈകിയത്?",
                    category = "Everyday conversation",
                    toneCategory = "Casual",
                    culturalNotes = "Phonetic fusion showing the gentle influence of the Tamil frontier.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 7. Thrissur
        RegionalDialect(
            id = "ml_in_kl_thrissur",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Central Kerala",
            cityOrArea = "Thrissur",
            dialectName = "Thrissur Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "ഗഡീ! എങ്ങനെയുണ്ട് തൃശൂർ പൂരം വിശേഷങ്ങൾ?",
                "എന്തൂട്ടാ ഈ പറയുന്നത്?",
                "മേളവും വെടിക്കെട്ടും കട്ട പൊരിഞ്ഞ സാധനമാണ്",
                "നമ്മള് വെറുതെ പറയുന്നതല്ല ട്ടോ"
            ),
            greeting = "എന്തൂട്ടാ ഗഡീ വിശേഷങ്ങൾ? തൃശൂർക്കാരൻ സംസാരിക്കുമ്പോൾ എനർജി വേറെ ലെവൽ ആയിരിക്കണം ട്ടോ!",
            localeCode = "ml-IN",
            description = "Punchy, rhythmic, rising intonation punctuated by famous expressions like 'Gadi' and 'Enthoottu'.",
            codeSwitchingDescription = "Blends festival banter with cinema-style everyday English.",
            tendenciesNotes = "Iconic intonation featuring signature vocabulary like 'ഗഡീ' (mate) and 'എന്തൂട്ടാ' (what on earth).",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ഗഡീ (Gadi)",
                    meaning = "ചങ്ങാതി / കൂട്ടുകാരൻ / സഹോദരൻ",
                    englishMeaning = "Pal, mate, buddy, bro",
                    region = "Kerala",
                    district = "Thrissur",
                    dialect = "Thrissur Style",
                    context = "Quintessential Thrissur casual address for peers and companions",
                    formalEquivalent = "സുഹൃത്തേ (Dear friend)",
                    casualEquivalent = "ചങ്ങാതി",
                    exampleSentence = "എന്തൂട്ടാ ഗഡീ നോക്കി നിൽക്കുന്നത്? ഇങ്ങോട്ട് കയറി ഇരിക്ക്!",
                    category = "Friendship",
                    toneCategory = "Friendly",
                    culturalNotes = "Heartbeat of Thrissur street conversations.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("മച്ചാൻ", "ചെങ്ങായി", "അളിയൻ")
                ),
                RegionalExpression(
                    expression = "എന്തൂട്ടാ (Enthoottu)",
                    meaning = "എന്താണ് / എന്ത് കാര്യമാണ്",
                    englishMeaning = "What on earth? / What is this?",
                    region = "Kerala",
                    district = "Thrissur",
                    dialect = "Thrissur Style",
                    context = "Expressing curiosity, playful disbelief, or astonishment",
                    formalEquivalent = "എന്താണ് സംഭവം (What is the matter)",
                    casualEquivalent = "എന്താണിത്",
                    exampleSentence = "എന്തൂട്ടാ നീ ഈ പറയുന്നത്? സത്യമാണോ ഇത്?",
                    category = "Surprise",
                    toneCategory = "Humorous",
                    culturalNotes = "The single most recognized Thrissur linguistic trademark.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 8. Ernakulam / Kochi
        RegionalDialect(
            id = "ml_in_kl_ernakulam",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Central Coastal",
            cityOrArea = "Ernakulam / Kochi",
            dialectName = "Kochi Coastal Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "മച്ചാനെ എന്താ പരിപാടി?",
                "നല്ലൊരു കൊച്ചി വൈബ് സീനാണ്",
                "പോർട്ട് സിറ്റിയിൽ അടിപൊളി ഫുഡ്",
                "കട്ട വെയിറ്റിംഗ് ആണല്ലോ ബ്രോ"
            ),
            greeting = "ഹലോ മച്ചാനെ! കൊച്ചിയുടെ ഹൃദയത്തിൽ നിന്ന് സ്വാഗതം! എന്തൊക്കെയുണ്ട് വിശേഷങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Fast, cosmopolitan coastal Malayalam packed with youth banter, Fort Kochi port heritage, and cinema slang.",
            codeSwitchingDescription = "Effortless Manglish blending English cinema jargon with authentic coastal Malayalam.",
            tendenciesNotes = "Modern youth slang hub, popularizing terms across Kerala through new-generation cinema.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "മച്ചാനെ (Machane)",
                    meaning = "സഹോദരാ / കൂട്ടുകാരാ",
                    englishMeaning = "Brother, pal, mate, close friend",
                    region = "Kerala",
                    district = "Ernakulam",
                    dialect = "Kochi Coastal Style",
                    context = "Standard friendly address in Kochi across all age groups",
                    formalEquivalent = "സുഹൃത്തേ (Suhruthe)",
                    casualEquivalent = "ബ്രോ",
                    exampleSentence = "മച്ചാനെ, നമ്മൾ ഇന്ന് വൈകുന്നേരം ഫോർട്ട് കൊച്ചിയിൽ മീറ്റ് ചെയ്യാം.",
                    category = "Friendship",
                    toneCategory = "Friendly",
                    culturalNotes = "Quintessential Kochi youth culture staple recognized across the globe.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("ബ്രോ", "അളിയാ", "ഗഡീ")
                ),
                RegionalExpression(
                    expression = "സീൻ (Scene)",
                    meaning = "സാഹചര്യം / സംഭവം / തീവ്രമായ അവസ്ഥ",
                    englishMeaning = "Situation, vibe, incident, or intense moment",
                    region = "Kerala",
                    district = "Ernakulam",
                    dialect = "Kochi Coastal Style",
                    context = "Describing a situation or an awesome occurrence",
                    formalEquivalent = "സാഹചര്യം (Sahacharyam)",
                    casualEquivalent = "അവസ്ഥ",
                    exampleSentence = "ഇന്നലത്തെ പരിപാടി ശരിക്കും ഒരു വേറെ ലെവൽ സീൻ ആയിരുന്നു!",
                    category = "Youth slang",
                    toneCategory = "Casual",
                    culturalNotes = "Cinema-born colloquialism now universal among Kerala youth.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 9. Idukki
        RegionalDialect(
            id = "ml_in_kl_idukki",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "High Ranges",
            cityOrArea = "Idukki",
            dialectName = "Idukki High-Range Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "ഹൈറേഞ്ചിലെ തണുപ്പ് അനുഭവിച്ചു തന്നെ അറിയണം",
                "ഏലത്തോട്ടത്തിൽ വിളവെടുപ്പ് തുടങ്ങി",
                "മലയോര പാതകളിൽ ഡ്രൈവിംഗ് ശ്രദ്ധിക്കണം",
                "ഇവിടുത്തെ കാറ്റ് ഒരു പ്രത്യേക അനുഭവമാണ്"
            ),
            greeting = "ഹൈറേഞ്ചിന്റെ കുളിരുള്ള മലനിരകളിൽ നിന്ന് സ്വാഗതം! എന്തുണ്ട് ഇടുക്കി വിശേഷങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Warm, hardworking high-range settler speech steeped in spice plantations, cool mists, and rugged resilience.",
            codeSwitchingDescription = "Agricultural & plantation vocabulary mixed with modern Malayalam.",
            tendenciesNotes = "Reflects the pioneer spirit of high-range farmers and crisp mountain conversational rhythm.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ഹൈറേഞ്ച് (High Range)",
                    meaning = "ഇടുക്കിയിലെ ഉയർന്ന മലനിരകൾ",
                    englishMeaning = "The high hill range of the Western Ghats",
                    region = "Kerala",
                    district = "Idukki",
                    dialect = "Idukki High-Range Style",
                    context = "Referring to life, agriculture, and culture of the Idukki hills",
                    formalEquivalent = "മലയോര മേഖല",
                    casualEquivalent = "മലമ്പ്രദേശം",
                    exampleSentence = "ഹൈറേഞ്ചിലെ കാപ്പി പൂക്കുന്ന മണം വേറെ തന്നെയാണ്.",
                    category = "Everyday conversation",
                    toneCategory = "Warm",
                    culturalNotes = "Symbolizes the pride and resilience of the cardamom hills.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 10. Kottayam
        RegionalDialect(
            id = "ml_in_kl_kottayam",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Central Travancore",
            cityOrArea = "Kottayam",
            dialectName = "Kottayam Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "അച്ചായൻ എന്താ പറയുന്നത്?",
                "നല്ലൊരു മീൻ കറിയും ചോറും കഴിച്ചാലോ?",
                "റബ്ബർ വെട്ടാൻ അതിരാവിലെ പോകണം",
                "കാര്യം വ്യക്തമായി സംസാരിക്കാം"
            ),
            greeting = "നമസ്കാരം! അക്ഷരനഗരിയുടെ മണ്ണിൽ നിന്ന് സ്വാഗതം! അച്ചായന്മാർക്കും ചേച്ചിമാർക്കും സുഖമല്ലേ?",
            localeCode = "ml-IN",
            description = "Articulate, resonant Central Travancore cadence celebrated in literature, rubber estates, and Christian heritage.",
            codeSwitchingDescription = "Literary Malayalam blended with church and plantation idioms.",
            tendenciesNotes = "Famous for honorifics like 'അച്ചായൻ' and precise, measured articulation.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "അച്ചായൻ (Achayan)",
                    meaning = "ജ്യേഷ്ഠൻ / ആദരണീയനായ മൂത്ത വ്യക്തി",
                    englishMeaning = "Elder brother / Respected male friend",
                    region = "Kerala",
                    district = "Kottayam",
                    dialect = "Kottayam Style",
                    context = "Affectionate, respectful form of address in Central Travancore",
                    formalEquivalent = "ജ്യേഷ്ഠൻ (Elder brother)",
                    casualEquivalent = "ചേട്ടൻ",
                    exampleSentence = "അച്ചായൻ പറഞ്ഞാൽ പിന്നെ അതിൽ ഒരു തർക്കവുമില്ല!",
                    category = "Family",
                    toneCategory = "Respectful",
                    culturalNotes = "Classic Central Travancore cultural identifier celebrated in regional folklore.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 11. Alappuzha
        RegionalDialect(
            id = "ml_in_kl_alappuzha",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Kuttanad & Coast",
            cityOrArea = "Alappuzha",
            dialectName = "Alappuzha Coastal Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "കായൽ കാറ്റേറ്റ് വള്ളത്തിൽ ഇരിക്കാം",
                "വള്ളംകളി കാണാൻ ആളുകൾ എത്തി തുടങ്ങി",
                "കരിമീൻ പൊള്ളിച്ചത് അടിപൊളിയാണ്",
                "കുട്ടനാട്ടിൽ വെള്ളം കയറിയോ സുഹൃത്തേ?"
            ),
            greeting = "വെനീസിന്റെയും കായലുകളുടെയും നാട്ടിലേക്ക് സ്വാഗതം! സുഖവിശേഷങ്ങൾ പങ്കുവെക്കാം.",
            localeCode = "ml-IN",
            description = "Musical, waterborne cadence influenced by backwaters, paddy boats, and coastal fisheries.",
            codeSwitchingDescription = "Backwater boating and fishing terms naturally integrated.",
            tendenciesNotes = "Lyrical, flowing rhythm echoing the cadence of Vanchipattu (boat songs).",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "വള്ളംകളി (Vallamkali)",
                    meaning = "ചുണ്ടൻ വള്ളങ്ങളുടെ പരമ്പരാഗത ജലമേള",
                    englishMeaning = "Traditional Snake Boat Race of Kuttanad",
                    region = "Kerala",
                    district = "Alappuzha",
                    dialect = "Alappuzha Coastal Style",
                    context = "Pride of Kuttanad culture and collective spirit",
                    formalEquivalent = "ജലോത്സവം (Water festival)",
                    casualEquivalent = "ബോട്ട് റേസ്",
                    exampleSentence = "നെഹ്‌റു ട്രോഫി വള്ളംകളി കാണാൻ നാട് മുഴുവൻ പുന്നമടയിൽ ഒത്തുകൂടി.",
                    category = "Traditional expressions",
                    toneCategory = "Festive",
                    culturalNotes = "The defining community festival of the backwater regions.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 12. Pathanamthitta
        RegionalDialect(
            id = "ml_in_kl_pathanamthitta",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Central Travancore Foothills",
            cityOrArea = "Pathanamthitta",
            dialectName = "Pathanamthitta Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "മലയോരങ്ങളിൽ നല്ല ശാന്തതയാണ്",
                "തീർത്ഥാടന കാലം തുടങ്ങിയല്ലോ",
                "ഇവിടുത്തെ ആളുകൾ വളരെ ശാന്തസ്വഭാവികളാണ്",
                "നമുക്ക് ഒത്തൊരുമിച്ച് കാര്യങ്ങൾ ചെയ്യാം"
            ),
            greeting = "തീർത്ഥാടന പുണ്യഭൂമിയിൽ നിന്ന് സ്നേഹാന്വേഷണങ്ങൾ! സുഖമായിരിക്കുന്നുവോ?",
            localeCode = "ml-IN",
            description = "Gentle, polite Central Travancore dialect marked by courteous honorifics and calm intonation.",
            codeSwitchingDescription = "Mixes polite traditional speech with contemporary terms.",
            tendenciesNotes = "Calm, temperate intonations shared across sacred river valleys and rolling hills.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "ശരണം (Sharanam)",
                    meaning = "അഭയം / അനുഗ്രഹം / സമാധാനം",
                    englishMeaning = "Refuge, divine grace, peaceful reassurance",
                    region = "Kerala",
                    district = "Pathanamthitta",
                    dialect = "Pathanamthitta Style",
                    context = "Spiritual ethos woven into pilgrim greeting culture",
                    formalEquivalent = "അഭയം (Refuge)",
                    casualEquivalent = "അനുഗ്രഹം",
                    exampleSentence = "യാത്ര സുരക്ഷിതമായിരിക്കട്ടെ, എല്ലാം നല്ലതിനാണ്.",
                    category = "Greetings",
                    toneCategory = "Polite",
                    culturalNotes = "Reflects the holy shrine heritage of Sabarimala and Aranmula.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 13. Kollam
        RegionalDialect(
            id = "ml_in_kl_kollam",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "South Coastal",
            cityOrArea = "Kollam",
            dialectName = "Kollam Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "അളിയാ! എന്തുവാടെ അവിടെ നടക്കുന്നത്?",
                "അഷ്ടമുടിക്കായലിൽ വൈകുന്നേരം നല്ല കാറ്റാണ്",
                "കശുവണ്ടി ഫാക്ടറികളിൽ പണി നടക്കുന്നു",
                "നീ വാ, നമുക്ക് ഒരുമിച്ച് സംസാരിക്കാം"
            ),
            greeting = "അളിയാ നമസ്കാരം! കൊല്ലം തുറമുഖ നഗരത്തിൽ നിന്ന് സ്വാഗതം! എന്തൊക്കെയുണ്ട് കാര്യങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Lively, energetic South Kerala coastal vernacular famous for affectionate address 'Aliya' and brisk sentence ends.",
            codeSwitchingDescription = "Harbor trade terms and lively street idioms.",
            tendenciesNotes = "Lively South Travancore cadence with words like 'അളിയാ' (mate/brother-in-law) and 'എന്തുവാടെ'.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "അളിയാ (Aliya)",
                    meaning = "മൈത്തുനൻ / അടുത്ത സുഹൃത്ത്",
                    englishMeaning = "Mate, brother-in-law, bosom buddy",
                    region = "Kerala",
                    district = "Kollam",
                    dialect = "Kollam Style",
                    context = "Universal affectionate address for peers in Kollam",
                    formalEquivalent = "സഹോദരാ / സുഹൃത്തേ",
                    casualEquivalent = "ചങ്ങാതി",
                    exampleSentence = "അളിയാ, നമ്മൾ ഇന്ന് അഷ്ടമുടി കായൽ തീരത്തു മീറ്റ് ചെയ്യാം.",
                    category = "Friendship",
                    toneCategory = "Friendly",
                    culturalNotes = "Deeply ingrained in Kollam youth camaraderie.",
                    verificationStatus = "Verified",
                    similarExpressions = listOf("മച്ചാൻ", "ഗഡീ")
                )
            )
        ),

        // 14. Thiruvananthapuram
        RegionalDialect(
            id = "ml_in_kl_thiruvananthapuram",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "South Travancore",
            cityOrArea = "Thiruvananthapuram",
            dialectName = "Thiruvananthapuram Style",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "എന്തുവാടെ വിശേഷം? സുഖന്തന്നെയാ?",
                "നമ്മുടെ കിഴക്കേക്കോട്ടയിൽ നല്ല തിരക്കാണ്",
                "അണ്ണൻ എന്താ ഒന്നും പറയാത്തത്?",
                "കാര്യം കിടിലനായിട്ട് നടന്നു കേട്ടോ"
            ),
            greeting = "എന്തുവാടെ വിശേഷം? തിരുവിതാംകൂറിന്റെ തലസ്ഥാനത്ത് നിന്ന് ഒരു കിടിലൻ സ്വാഗതം!",
            localeCode = "ml-IN",
            description = "Rapid, witty, distinctive South Travancore dialect with unique verb endings like '-ade', '-uva', and capital banter.",
            codeSwitchingDescription = "Mixes political, government, cinema, and university slang with southern idioms.",
            tendenciesNotes = "Characterized by interrogative 'എന്തുവാടെ' (what is up), honorific 'അണ്ണാ', and royal/market slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "എന്തുവാടെ (Enthuvade)",
                    meaning = "എന്താണ് കൂട്ടുകാരാ / എന്ത് പറ്റി",
                    englishMeaning = "What is up mate? / What's going on?",
                    region = "Kerala",
                    district = "Thiruvananthapuram",
                    dialect = "Thiruvananthapuram Style",
                    context = "Casual friendly inquiry when encountering familiar friends",
                    formalEquivalent = "എന്താണ് വിശേഷം (What is the news)",
                    casualEquivalent = "എന്താ വിശേഷം",
                    exampleSentence = "എന്തുവാടെ, ഇന്നലെ വിളിച്ചിട്ട് ഫോൺ എടുക്കാഞ്ഞത്?",
                    category = "Greetings",
                    toneCategory = "Casual",
                    culturalNotes = "Signature conversational opening throughout the capital city.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "കിടിലൻ (Kidilan)",
                    meaning = "വളരെ മികച്ചത് / ഗംഭീരമായത്",
                    englishMeaning = "Awesome, fantastic, stellar",
                    region = "Kerala",
                    district = "Thiruvananthapuram",
                    dialect = "Thiruvananthapuram Style",
                    context = "Expressing high admiration for a performance, meal, or event",
                    formalEquivalent = "ഗംഭീരം / ഉത്തമം",
                    casualEquivalent = "സൂപ്പർ",
                    exampleSentence = "ഇന്നലത്തെ പരിപാടി ശരിക്കും ഒരു കിടിലൻ അനുഭവമായിരുന്നു!",
                    category = "Happiness",
                    toneCategory = "Enthusiastic",
                    culturalNotes = "Capital city slang now embraced all over Malayalam cinema and speech.",
                    verificationStatus = "Verified"
                )
            )
        ),

        // 15. Kerala — General Malayalam
        RegionalDialect(
            id = "ml_in_kl_general",
            language = "Malayalam",
            country = "India",
            stateOrProvince = "Kerala",
            region = "Kerala State",
            cityOrArea = "Kerala (General)",
            dialectName = "General Malayalam",
            flagEmoji = "🇮🇳",
            samplePhrases = listOf(
                "നമസ്കാരം, എങ്ങനെയുണ്ട് വിശേഷങ്ങൾ?",
                "നല്ലൊരു ദിവസം ആശംസിക്കുന്നു",
                "നമുക്ക് വിശദമായി സംസാരിക്കാം",
                "ഇക്കാര്യത്തിൽ ഞാൻ പൂർണ്ണമായും സഹായിക്കാം"
            ),
            greeting = "നമസ്കാരം! കേരളത്തിലെവിടെയും സംസാരിക്കാവുന്ന ലളിതവും മനോഹരവുമായ മലയാളത്തിലേക്ക് സ്വാഗതം. എന്തുണ്ട് വിശേഷങ്ങൾ?",
            localeCode = "ml-IN",
            description = "Clean, natural, state-wide Malayalam without strong district-specific slang, ideal for neutral conversation.",
            codeSwitchingDescription = "Effortless natural conversational Malayalam, balancing standard vocabulary with everyday ease.",
            tendenciesNotes = "Balanced and welcoming across all regions without leaning into single-district slang.",
            typicalExpressions = listOf(
                RegionalExpression(
                    expression = "നമസ്കാരം (Namaskaram)",
                    meaning = "പരമ്പരാഗത വന്ദനം",
                    englishMeaning = "Traditional respectful greeting / Hello",
                    region = "Kerala",
                    district = "General",
                    dialect = "General Malayalam",
                    context = "Universal respectful and warm greeting across all ages",
                    formalEquivalent = "നമസ്കാരം",
                    casualEquivalent = "ഹലോ",
                    exampleSentence = "എല്ലാവർക്കും എന്റെ ഹൃദയം നിറഞ്ഞ നമസ്കാരം.",
                    category = "Greetings",
                    toneCategory = "Polite",
                    culturalNotes = "Universal Kerala greeting.",
                    verificationStatus = "Verified"
                ),
                RegionalExpression(
                    expression = "സന്തോഷം (Santhosham)",
                    meaning = "ആനന്ദം / സന്തോഷം പങ്കുവെക്കൽ",
                    englishMeaning = "Pleasure, joy, happiness to connect",
                    region = "Kerala",
                    district = "General",
                    dialect = "General Malayalam",
                    context = "Expressing gratitude and gladness during a conversation",
                    formalEquivalent = "വളരെ സന്തോഷം",
                    casualEquivalent = "ഹാപ്പി",
                    exampleSentence = "നിങ്ങളുമായി സംസാരിക്കാൻ സാധിച്ചതിൽ വളരെ സന്തോഷം.",
                    category = "Happiness",
                    toneCategory = "Warm",
                    culturalNotes = "The essence of hospitable Malayali communication.",
                    verificationStatus = "Verified"
                )
            )
        )
    )
}
