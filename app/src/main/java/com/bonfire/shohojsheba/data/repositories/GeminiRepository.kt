package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.BuildConfig
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.remote.LocalizedString
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import java.util.Locale

class GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite", // Using a standard model
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f
            topK = 32
            topP = 1f
            maxOutputTokens = 4096
            responseMimeType = "application/json"
        }
    )

    // This function generates a full Service object from a simple query string.
    // It returns a 'Flow' because network requests take time and we want to emit the result when ready.
    fun generateService(query: String): Flow<Pair<Service, ServiceDetail>?> = flow {
        try {
            // 1. Create a very specific instruction (Prompt) for the AI
            val prompt = createPrompt(query)
            
            // 2. Send it to Gemini
            val response = generativeModel.generateContent(prompt)
            val text = response.text

            if (!text.isNullOrBlank()) {
                // 3. Convert the AI's text response (JSON) into Kotlin objects
                val result = parseResponse(text, query)
                emit(result)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO) // Run this on a background thread (IO) to avoid freezing the app


    // --- Prompt Engineering ---
    // We give the AI a "Persona" (Expert Assistant) and very strict rules.
    // We force it to return JSON format so our code can read it easily.
    private fun createPrompt(query: String): String {
        return """
            You are an expert assistant for Bangladeshi government and essential services.
            
            IMPORTANT: Create ONLY ONE specific service entry for: "$query"
            - Do NOT create variations or similar services
            - Focus on the EXACT service requested
            - All information must be specific to BANGLADESH only
            - Use official Bangladesh government procedures and requirements
            - Provide DETAILED, COMPREHENSIVE information
            
            CATEGORIZATION RULES - Strictly categorize this service into EXACTLY ONE of these 4 categories:
            
            ═══════════════════════════════════════════════════════════════════
            CATEGORY 1: "citizen" - Personal administrative services for individuals
            ═══════════════════════════════════════════════════════════════════
            
            DOCUMENTS & IDENTITY:
            - NID (National ID Card), Smart NID, NID correction, NID renewal
            - Passport (পাসপোর্ট), Passport renewal, e-Passport, MRP Passport
            - Birth Registration (জন্ম নিবন্ধন), Birth Certificate, Late birth registration
            - Death Certificate (মৃত্যু সনদ), Death Registration
            - Marriage Registration (বিবাহ নিবন্ধন), Marriage Certificate, Divorce Certificate
            - Citizenship Certificate (নাগরিকত্ব সনদ)
            - Educational certificates, Marksheet collection, Character certificates
            - Medical certificates (personal)
            
            LICENSES & CLEARANCES (Personal):
            - Driving License (ড্রাইভিং লাইসেন্স), Learner's Permit, License renewal
            - Arms License (personal firearms)
            - Police Clearance Certificate, Police Verification
            - Good Conduct Certificate, Character Certificate
            
            PERSONAL FINANCE & BANKING:
            - Personal income tax return (for individual salaried employees ONLY)
            - Personal bank account opening (savings/current for individuals)
            - Personal loan (home loan, car loan, education loan for personal use)
            
            UTILITIES & HOUSEHOLD:
            - Electricity connection (বিদ্যুৎ সংযোগ) for home
            - Gas connection (গ্যাস সংযোগ) for residential use
            - Water connection (পানি সংযোগ) for household
            - Ration card (রেশন কার্ড), Food card, VGF card
            - Government hospital services, Health card
            - School/College admission for dependents
            
            ═══════════════════════════════════════════════════════════════════
            CATEGORY 2: "farmer" - Agriculture, livestock, fisheries & LAND services
            ═══════════════════════════════════════════════════════════════════
            
            🔴 LAND SERVICES (MOST CRITICAL - Highest priority):
            
            English Keywords: land, land mutation, land transfer, land registration, 
            khatian, porcha, deed, plot, boundary, survey, inheritance, lease, ownership
            
            Bengali Keywords: জমি, ভূমি, নামজারি, খতিয়ান, পর্চা, দলিল, প্লট, 
            জরিপ, উত্তরাধিকার, বন্দোবস্ত, মালিকানা, সীমানা
            
            Specific Land Services:
            - Land mutation (নামজারি) - name transfer in land records
            - Land transfer (জমি হস্তান্তর), Land ownership transfer
            - Khatian (খতিয়ান) - land ownership records
            - Porcha (পর্চা) - land record certificate
            - RS Khatian, SA Khatian, BS Khatian, City Survey records
            - Deed registration (দলিল রেজিস্ট্রেশন), Land deed
            - Land survey (জরিপ), Plot measurement, Boundary demarcation
            - Agricultural land lease (কৃষি জমি বন্দোবস্ত)
            - Land inheritance (উত্তরাধিকার), Batwara (বাটোয়ারা - partition)
            - DCR (Land Development Tax) payment
            - Land tax payment, Agricultural land tax
            - Bhumi Unnayan Kor (ভূমি উন্নয়ন কর)
            - Land clearing certificate, Dakhila (দাখিলা)
            - Non-judicial stamp for land, Mouza map
            
            FARMING & AGRICULTURE:
            - Agricultural subsidies (কৃষি ভর্তুকি), Seed subsidies, Fertilizer subsidies
            - Crop insurance (ফসল বীমা), Weather-based insurance
            - Irrigation facilities (সেচ সুবিধা), Deep tube well permits
            - Agricultural loans (কৃষি ঋণ), Farmer credit cards, Krishi Bank loans
            - Farming equipment subsidies, Tractor subsidies
            - Crop damage compensation, Natural disaster relief for farmers
            - Agricultural training programs, Farmer registration
            
            LIVESTOCK & FISHERIES:
            - Livestock registration (গবাদি পশু), Cow/Buffalo registration
            - Veterinary services (পশু চিকিৎসা), Animal health card
            - Poultry permits (হাঁস-মুরগি পালন), Poultry farm license
            - Fishery licenses (মৎস্য চাষ), Fish farming permits
            - Dairy farm registration, Milk collection center license
            
            ═══════════════════════════════════════════════════════════════════
            CATEGORY 3: "entrepreneur" - Business, trade, commerce & taxation
            ═══════════════════════════════════════════════════════════════════
            
            BUSINESS REGISTRATION & LICENSES:
            - Trade License (ট্রেড লাইসেন্স), Trade License renewal
            - Business registration (ব্যবসা নিবন্ধন), Company incorporation
            - Partnership deed (অংশীদারি দলিল), Proprietorship registration
            - Shop license (দোকান লাইসেন্স), Factory license, Industry license
            - RJSC registration (Registrar of Joint Stock Companies)
            - NGO registration (এনজিও নিবন্ধন), Society registration
            - Restaurant license, Food business license
            
            TAX & FINANCIAL SERVICES (Business):
            🔴 CRITICAL - Tax services for business are NOT citizen services:
            - TIN registration (টিন), E-TIN registration, TIN certificate
            - VAT registration (ভ্যাট), VAT return submission, VAT certificate
            - Mushak (মুসক) 6.3, 9.1, 9.3 forms
            - BIN (Business Identification Number)
            - Business tax services, Corporate tax, Income tax for business
            - Tax clearance certificate for business
            - Customs duty payment, Import duty, Export duty
            
            TRADE & IMPORT/EXPORT:
            - Import Registration Certificate (IRC), Export Registration Certificate (ERC)
            - Customs clearance (শুল্ক ছাড়পত্র), Bonded warehouse license
            - C&F agent license, Clearing agent registration
            - Export subsidy, Export incentives
            - Foreign trade licensing
            
            FINANCING & INVESTMENT:
            - Commercial bank loan, SME loans (এসএমই), Business loans
            - Investment board registration, Investment permit
            - Entrepreneur development loan, Startup registration
            - Industrial loan, Working capital loan
            
            INTELLECTUAL PROPERTY & CERTIFICATION:
            - Brand registration (ব্র্যান্ড), Patent (পেটেন্ট), Trademark (ট্রেডমার্ক)
            - Copyright registration, Design registration
            - BSTI certification (product standards), ISO certification
            - Halal certification for products, Quality certification
            
            SPECIALIZED BUSINESS SERVICES:
            - Commercial land/property registration (for business use)
            - Signboard permission for business, Advertisement permit
            - Environmental clearance (for factories/industries)
            - Fire license for commercial establishments
            
            ═══════════════════════════════════════════════════════════════════
            CATEGORY 4: "govt_office" - Exclusively for government employees
            ═══════════════════════════════════════════════════════════════════
            
            RETIREMENT & BENEFITS:
            - Pension application (পেনশন), Pension processing
            - GPF (General Provident Fund), GPF withdrawal
            - Gratuity, Leave encashment
            - Benevolent fund, Welfare fund
            
            EMPLOYMENT & SERVICE:
            - Service book (সার্ভিস বুক), Service certificate
            - Government job recruitment, BCS application
            - Transfer orders (বদলি), Posting orders
            - Leave applications (ছুটি), Earned leave, Medical leave
            - Government employee ID, Office order
            - Promotion orders, Increment certificate
            - ACR (Annual Confidential Report)
            
            ═══════════════════════════════════════════════════════════════════
            DECISION ALGORITHM (Follow strictly in this exact order):
            ═══════════════════════════════════════════════════════════════════
            
            STEP 1: LAND SERVICE CHECK (Highest Priority)
            Query contains ANY of these keywords?
            - English: land, mutation, khatian, porcha, deed, plot, survey, boundary, inheritance, lease, ownership, batwara, dakhila, DCR, mouza
            - Bengali: জমি, ভূমি, নামজারি, খতিয়ান, পর্চা, দলিল, প্লট, জরিপ, সীমানা, উত্তরাধিকার, বাটোয়ারা, দাখিলা
            → IF YES: Category = "farmer" (FINAL, DO NOT RECONSIDER)
            
            STEP 2: BUSINESS/TAX SERVICE CHECK
            Query contains ANY of these keywords?
            - Tax/Finance: TIN, ETIN, E-TIN, VAT, Mushak, BIN, business tax, corporate tax, টিন, ভ্যাট, মুসক
            - Business: trade license, business registration, company, IRC, ERC, trademark, patent, BSTI, ট্রেড লাইসেন্স, ব্যবসা
            - Commerce: import, export, customs, SME, entrepreneur, startup, এসএমই
            → IF YES: Category = "entrepreneur" (FINAL, DO NOT RECONSIDER)
            
            STEP 3: AGRICULTURE/FARMING CHECK
            Query contains ANY of these keywords?
            - Farming: agricultural, crop, farming, irrigation, fertilizer, seed, কৃষি, ফসল, সেচ
            - Livestock: livestock, cattle, poultry, fishery, dairy, গবাদি পশু, মৎস্য, হাঁস-মুরগি
            → IF YES: Category = "farmer" (FINAL)
            
            STEP 4: GOVERNMENT EMPLOYEE CHECK
            Query contains ANY of these keywords?
            - pension, GPF, service book, BCS, government job, posting, transfer, পেনশন, সার্ভিস বুক, বদলি
            → IF YES: Category = "govt_office" (FINAL)
            
            STEP 5: DEFAULT TO CITIZEN
            If none of the above conditions are met:
            → Category = "citizen"
            
            ═══════════════════════════════════════════════════════════════════
            CRITICAL VALIDATION RULES (Triple-check before finalizing):
            ═══════════════════════════════════════════════════════════════════
            
            ✅ RULE 1: ANY land/property service → "farmer" (NOT citizen, NOT entrepreneur)
            ✅ RULE 2: TIN/E-TIN/VAT registration → "entrepreneur" (NOT citizen)
            ✅ RULE 3: Trade License → "entrepreneur" (NOT citizen)
            ✅ RULE 4: Business tax services → "entrepreneur" (NOT citizen)
            ✅ RULE 5: Personal tax return (salaried only) → "citizen" (NOT entrepreneur)
            ✅ RULE 6: Agriculture/farming/livestock → "farmer"
            ✅ RULE 7: Government employee-only services → "govt_office"
            ✅ RULE 8: Land mutation/khatian/deed → "farmer" (ALWAYS, zero exceptions)
            
            ═══════════════════════════════════════════════════════════════════
            COMMON DEMO SCENARIOS - TEST CASES:
            ═══════════════════════════════════════════════════════════════════
            
            Query: "Apply for land mutation" / "নামজারি আবেদন"
            ✅ CORRECT Category: "farmer"
            ❌ WRONG: "citizen"
            
            Query: "E-TIN registration" / "ই-টিন নিবন্ধন"
            ✅ CORRECT Category: "entrepreneur"
            ❌ WRONG: "citizen"
            
            Query: "Trade License" / "ট্রেড লাইসেন্স"
            ✅ CORRECT Category: "entrepreneur"
            ❌ WRONG: "citizen"
            
            Query: "Khatian copy" / "খতিয়ান কপি"
            ✅ CORRECT Category: "farmer"
            ❌ WRONG: "citizen"
            
            Query: "VAT registration" / "ভ্যাট নিবন্ধন"
            ✅ CORRECT Category: "entrepreneur"
            ❌ WRONG: "citizen"
            
            Query: "Passport application" / "পাসপোর্ট আবেদন"
            ✅ CORRECT Category: "citizen"
            
            Query: "Land deed registration" / "জমির দলিল"
            ✅ CORRECT Category: "farmer"
            ❌ WRONG: "citizen" or "entrepreneur"
            
            Query: "Agricultural subsidy" / "কৃষি ভর্তুকি"
            ✅ CORRECT Category: "farmer"
            
            Query: "Business registration" / "ব্যবসা নিবন্ধন"
            ✅ CORRECT Category: "entrepreneur"
            ❌ WRONG: "citizen"
            
            Query: "Pension application" / "পেনশন"
            ✅ CORRECT Category: "govt_office"
            ❌ WRONG: "citizen"

            Return strictly valid JSON with NO markdown formatting. Use this exact structure:
            {
              "service": {
                "title_en": "Exact English title for the requested service",
                "title_bn": "Exact Bengali title",
                "subtitle_en": "Short English description",
                "subtitle_bn": "Short Bengali description",
                "category": "citizen", 
                "searchKeywords": "keyword1, keyword2, keyword3"
              },
              "details": {
                "instructions_en": "DETAILED step-by-step instructions for Bangladesh. Include:
                - Where to go (specific office/website)
                - What forms to fill
                - Exact procedure steps (numbered with blank line after each step)
                - Fees and payment methods
                - Important tips and notes
                
                FORMATTING REQUIREMENT: When providing numbered steps, add a blank line after each step for readability.
                Example:
                Step 1: First instruction here
                
                Step 2: Second instruction here
                
                Step 3: Third instruction here
                
                Make this comprehensive and helpful.",
                
                "instructions_bn": "বাংলাদেশের জন্য বিস্তারিত ধাপে ধাপে নির্দেশাবলী। অন্তর্ভুক্ত করুন:
                - কোথায় যেতে হবে (নির্দিষ্ট অফিস/ওয়েবসাইট)
                - কোন ফর্ম পূরণ করতে হবে
                - সঠিক পদ্ধতির ধাপ (সংখ্যাযুক্ত এবং প্রতিটি ধাপের পরে একটি ফাঁকা লাইন)
                - ফি এবং পেমেন্ট পদ্ধতি
                - গুরুত্বপূর্ণ টিপস এবং নোট
                
                ফরম্যাটিং প্রয়োজনীয়তা: সংখ্যাযুক্ত ধাপ প্রদান করার সময়, পাঠযোগ্যতার জন্য প্রতিটি ধাপের পরে একটি ফাঁকা লাইন যোগ করুন।
                উদাহরণ:
                ধাপ ১: এখানে প্রথম নির্দেশাবলী
                
                ধাপ ২: এখানে দ্বিতীয় নির্দেশাবলী
                
                ধাপ ৩: এখানে তৃতীয় নির্দেশাবলী
                
                এটি ব্যাপক এবং সহায়ক করুন।",
                
                "requiredDocuments_en": "Complete list of ALL documents required in Bangladesh. For each document, specify:
                - Document name
                - Number of copies needed
                - Original or photocopy
                - Any special requirements",
                
                "requiredDocuments_bn": "বাংলাদেশে প্রয়োজনীয় সমস্ত নথির সম্পূর্ণ তালিকা। প্রতিটি নথির জন্য উল্লেখ করুন:
                - নথির নাম
                - প্রয়োজনীয় কপির সংখ্যা
                - মূল বা ফটোকপি
                - কোন বিশেষ প্রয়োজনীয়তা",
                
                "processingTime_en": "Realistic processing time in Bangladesh (e.g., 3-5 working days, 2 weeks, etc.)",
                "processingTime_bn": "বাংলাদেশে বাস্তবসম্মত প্রক্রিয়াকরণ সময় (যেমন, ৩-৫ কার্যদিবস, ২ সপ্তাহ, ইত্যাদি)",
                
                "contactInfo_en": "Bangladesh government contact information:
                - Office name and address
                - Phone numbers
                - Email addresses
                - Official website URL
                - Helpline numbers if available",
                
                "contactInfo_bn": "বাংলাদেশ সরকারের যোগাযোগের তথ্য:
                - অফিসের নাম এবং ঠিকানা
                - ফোন নম্বর
                - ইমেল ঠিকানা
                - অফিসিয়াল ওয়েবসাইট URL
                - হেল্পলাইন নম্বর যদি থাকে",
                
                "youtubeLink": "optional youtube link or null"
              }
            }
            
            Remember: 
            - Create ONLY ONE service, specific to Bangladesh government procedures
            - Be DETAILED and COMPREHENSIVE in all fields
            - Provide ACTIONABLE information that users can actually follow
            - Include specific office names, addresses, and contact information where possible
            - Add blank lines between numbered steps for better readability
        """.trimIndent()
    }

    // --- JSON Parsing ---
    // Converts the raw text response from Gemini into our App's data models (Service & ServiceDetail)
    private fun parseResponse(jsonString: String, originalQuery: String): Pair<Service, ServiceDetail>? {
        return try {
            // 1. Clean the JSON (remove markdown code blocks if present)
            val cleanJson = sanitizeJson(jsonString)
            val root = JSONObject(cleanJson)
            
            val serviceObj = root.getJSONObject("service")
            val detailsObj = root.getJSONObject("details")
            
            // 2. Extract Fields safely (using optString to avoid crashes if fields are missing)
            val category = serviceObj.optString("category", "citizen").lowercase().trim()
            val titleEn = serviceObj.optString("title_en", originalQuery)
            
            // 3. Generate a unique ID for this new service
            val id = generateId(titleEn, category)
            val timestamp = System.currentTimeMillis()
            
            // 4. Create Service Entity (for the list view)
            val service = Service(
                id = id,
                title = LocalizedString(serviceObj.optString("title_en"), serviceObj.optString("title_bn")),
                subtitle = LocalizedString(serviceObj.optString("subtitle_en"), serviceObj.optString("subtitle_bn")),
                iconName = "info", // Default icon for AI results
                category = category,
                versionAdded = 1,
                lastUpdated = timestamp,
                images = "",
                imageNames = "",
                searchKeywords = serviceObj.optString("searchKeywords")
            )
            
            // 5. Create ServiceDetail Entity (for the detailed view)
            val serviceDetail = ServiceDetail(
                serviceId = id,
                instructions = LocalizedString(detailsObj.optString("instructions_en"), detailsObj.optString("instructions_bn")),
                requiredDocuments = LocalizedString(detailsObj.optString("requiredDocuments_en"), detailsObj.optString("requiredDocuments_bn")),
                processingTime = LocalizedString(detailsObj.optString("processingTime_en"), detailsObj.optString("processingTime_bn")),
                contactInfo = LocalizedString(detailsObj.optString("contactInfo_en"), detailsObj.optString("contactInfo_bn")),
                youtubeLink = detailsObj.optString("youtubeLink").takeIf { it.isNotEmpty() && it != "null" },
                lastUpdated = timestamp,
                imageNames = "",
                images = ""
            )
            
            Pair(service, serviceDetail)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if parsing fails
        }
    }

    // --- Helper: Sanitize JSON ---
    // AI often wraps JSON in markdown code blocks (```json ... ```).
    // This function strips those out to get the raw JSON string.
    private fun sanitizeJson(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substring(7)
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3)
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length - 3)
        }
        return clean.trim()
    }

    private fun generateId(name: String, category: String): String {
        val normalizedName = name.lowercase(Locale.ROOT)
            .trim()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        
        return "${category}_${normalizedName}"
    }
}
