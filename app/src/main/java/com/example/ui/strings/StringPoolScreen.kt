package com.example.ui.strings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StringCategory
import com.example.data.model.StringEntry
import com.example.ui.components.CyberCard
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SmaliPurple
import com.example.ui.theme.TextMuted

@Composable
fun StringPoolScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(StringCategory.ALL) }
    var expandedStringId by remember { mutableStateOf<Int?>(null) }

    val allStrings = currentAnalysis?.strings ?: emptyList()

    val filteredStrings = remember(allStrings, searchQuery, selectedCategory) {
        allStrings.filter { entry ->
            val matchCategory = selectedCategory == StringCategory.ALL || entry.category == selectedCategory
            val matchQuery = searchQuery.isBlank() || entry.value.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isArabic) "مجمع نصوص الحزمة (String Pool & XREFs)" else "DEX String Pool & XREFs",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${filteredStrings.size} / ${allStrings.size} ${if (isArabic) "نص مفهرس" else "strings indexed"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("string_search_input"),
                placeholder = {
                    Text(
                        text = if (isArabic) "البحث في مجمع النصوص..." else "Search strings, URLs, endpoints...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = CyberCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCardBorder
                ),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StringCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = if (isArabic) cat.labelAr else cat.labelEn,
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
        }

        if (filteredStrings.isEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isArabic) "لا توجد نتائج مطابقة لبحثك" else "No matching strings found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(filteredStrings.take(150), key = { it.id }) { item ->
                val isExpanded = expandedStringId == item.id

                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedStringId = if (isExpanded) null else item.id
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = getCategoryColor(item.category).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isArabic) item.category.labelAr else item.category.labelEn,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getCategoryColor(item.category),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "XREF: ${item.xrefCount}",
                                    fontSize = 11.sp,
                                    fontFamily = CodeFontFamily,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(item.value))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.value,
                            fontSize = 13.sp,
                            fontFamily = CodeFontFamily,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // XREF Call locations if expanded
                        AnimatedVisibility(visible = isExpanded && item.xrefLocations.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "مواقع الاستدعاء (XREF References):" else "XREF Reference Locations:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                item.xrefLocations.forEach { loc ->
                                    Text(
                                        text = "• $loc",
                                        fontSize = 11.sp,
                                        fontFamily = CodeFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getCategoryColor(category: StringCategory) = when (category) {
    StringCategory.URL, StringCategory.DOMAIN -> CyberCyan
    StringCategory.API -> SeverityInfo
    StringCategory.BILLING -> SeverityHigh
    StringCategory.SUBSCRIPTION -> SmaliPurple
    StringCategory.SECURITY -> SeverityCritical
    StringCategory.EMAIL -> SeverityMedium
    StringCategory.SYSTEM -> SeverityLow
    else -> TextMuted
}
