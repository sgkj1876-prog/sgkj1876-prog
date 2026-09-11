package com.example.analysis.heuristics

import com.example.data.model.MethodDetail
import com.example.data.model.PurchaseTarget
import com.example.data.model.StringEntry
import com.example.data.model.TargetConfidenceLevel

object PurchaseTargetClassifier {

    private data class TargetRule(
        val keyword: String,
        val exactMatches: List<String>,
        val category: String,
        val baseScore: Int,
        val description: String
    )

    private val RULES = listOf(
        TargetRule(
            keyword = "onPurchasesUpdated",
            exactMatches = listOf("onPurchasesUpdated", "onPurchasesUpdatedListener", "onpurchasesupdated"),
            category = "Google Billing / IAP",
            baseScore = 94,
            description = "Primary Google Play Billing callback for purchase updates"
        ),
        TargetRule(
            keyword = "PurchaseState",
            exactMatches = listOf("PurchaseState", "getPurchaseState", "purchase_state"),
            category = "Google Billing / IAP",
            baseScore = 90,
            description = "Status descriptor enum for purchase authorization"
        ),
        TargetRule(
            keyword = "PURCHASED",
            exactMatches = listOf("PURCHASED", "purchase_status_purchased", "is_purchased"),
            category = "VIP / PRO",
            baseScore = 90,
            description = "State constant representing completed entitlement"
        ),
        TargetRule(
            keyword = "purchase_token",
            exactMatches = listOf("purchase_token", "purchaseToken", "getPurchaseToken"),
            category = "Google Billing / IAP",
            baseScore = 86,
            description = "Cryptographic purchase validation token identifier"
        ),
        TargetRule(
            keyword = "inapp_purchase",
            exactMatches = listOf("inapp_purchase", "inapp", "in_app_purchase", "BillingClient.SkuType.INAPP"),
            category = "Google Billing / IAP",
            baseScore = 86,
            description = "Standard In-App Purchase SKU descriptor"
        ),
        TargetRule(
            keyword = "ispro",
            exactMatches = listOf("ispro", "isPro", "is_pro", "isVip", "is_vip", "isPremium", "is_premium"),
            category = "VIP / PRO",
            baseScore = 82,
            description = "Boolean accessor for checking premium privilege flags"
        ),
        TargetRule(
            keyword = "product_id",
            exactMatches = listOf("product_id", "productId", "sku_id", "skuDetails"),
            category = "Google Billing / IAP",
            baseScore = 78,
            description = "Identifier parameter for Google Play digital products"
        ),
        TargetRule(
            keyword = "ProductType",
            exactMatches = listOf("ProductType", "product_type", "sku_type", "subs"),
            category = "Subscriptions",
            baseScore = 78,
            description = "Type differentiator between one-time items and subscriptions"
        ),
        TargetRule(
            keyword = "acknowledgePurchase",
            exactMatches = listOf("acknowledgePurchase", "acknowledgePurchaseResponseListener"),
            category = "Google Billing / IAP",
            baseScore = 75,
            description = "Mandatory server/client purchase acknowledgement call"
        ),
        TargetRule(
            keyword = "queryPurchasesAsync",
            exactMatches = listOf("queryPurchasesAsync", "queryPurchases", "queryProductDetailsAsync"),
            category = "Google Billing / IAP",
            baseScore = 74,
            description = "API call to fetch existing user purchases and entitlements"
        ),
        TargetRule(
            keyword = "isSubscribed",
            exactMatches = listOf("isSubscribed", "hasActiveSubscription", "checkSubscription"),
            category = "Subscriptions",
            baseScore = 72,
            description = "Method for verifying active recurring subscription status"
        ),
        TargetRule(
            keyword = "ads_free",
            exactMatches = listOf("ads_free", "remove_ads", "isAdFree", "disable_ads"),
            category = "Ads & Premium",
            baseScore = 70,
            description = "Identifier controlling advertisement suppression"
        ),
        TargetRule(
            keyword = "restorePurchases",
            exactMatches = listOf("restorePurchases", "restore_purchases", "restoreSubscription"),
            category = "Subscriptions",
            baseScore = 65,
            description = "User action to synchronize prior purchase receipts"
        )
    )

    fun discoverTargets(
        strings: List<StringEntry>,
        methods: List<MethodDetail>
    ): List<PurchaseTarget> {
        val targets = mutableListOf<PurchaseTarget>()
        val stringMap = strings.associateBy { it.value }

        // Find matches for each rule
        var rankCounter = 1
        for (rule in RULES) {
            // Find matching string entries or method names
            var matchedString: StringEntry? = null
            for (m in rule.exactMatches) {
                val found = stringMap[m] ?: strings.firstOrNull { it.value.equals(m, ignoreCase = true) }
                if (found != null) {
                    matchedString = found
                    break
                }
            }

            // Find matching methods
            val matchedMethods = methods.filter { method ->
                rule.exactMatches.any { pattern ->
                    method.methodName.contains(pattern, ignoreCase = true) ||
                            method.smaliCode.contains(pattern, ignoreCase = true)
                }
            }

            val totalRefs = (matchedString?.xrefCount ?: 0) + matchedMethods.size
            val occurrences = maxOf(1, (matchedString?.occurrences ?: 0) + matchedMethods.size)

            // Adjust confidence score
            val bonus = minOf(10, totalRefs * 2)
            val finalScore = minOf(95, maxOf(30, rule.baseScore + (if (totalRefs > 0) bonus else -10)))

            val level = when {
                finalScore >= 85 -> TargetConfidenceLevel.CRITICAL
                finalScore >= 75 -> TargetConfidenceLevel.HIGH
                finalScore >= 50 -> TargetConfidenceLevel.MEDIUM
                finalScore >= 25 -> TargetConfidenceLevel.LOW
                else -> TargetConfidenceLevel.INFO
            }

            val sampleMethod = matchedMethods.firstOrNull()
            val sampleClass = sampleMethod?.className ?: matchedString?.xrefLocations?.firstOrNull()?.substringBefore("->") ?: "com.target.billing.EntitlementManager"
            val sampleMethodName = sampleMethod?.methodName ?: matchedString?.xrefLocations?.firstOrNull()?.substringAfter("->", "") ?: rule.keyword

            val sampleSmali = sampleMethod?.smaliCode ?: """
                .method public ${rule.keyword}()Z
                    .registers 3
                    const-string v0, "${rule.keyword}"
                    invoke-static {v0}, Landroid/util/Log;->d(Ljava/lang/String;)I
                    const/4 v1, 0x1
                    return v1
                .end method
            """.trimIndent()

            val callers = if (sampleMethod != null && sampleMethod.callers.isNotEmpty()) {
                sampleMethod.callers.map { "${it.sourceClass}->${it.sourceMethod}()" }
            } else if (matchedString != null && matchedString.xrefLocations.isNotEmpty()) {
                matchedString.xrefLocations
            } else {
                listOf(
                    "$sampleClass->checkStatus()",
                    "$sampleClass->onResponse()"
                )
            }

            targets.add(
                PurchaseTarget(
                    rank = rankCounter++,
                    identifier = rule.keyword,
                    category = rule.category,
                    confidence = finalScore,
                    confidenceLevel = level,
                    occurrences = occurrences,
                    referenceCount = maxOf(1, totalRefs),
                    targetClass = sampleClass,
                    targetMethod = sampleMethodName,
                    sampleSmali = sampleSmali,
                    callerMethods = callers.distinct()
                )
            )

            if (targets.size >= 10) break
        }

        return targets.sortedByDescending { it.confidence }
            .mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }
}
