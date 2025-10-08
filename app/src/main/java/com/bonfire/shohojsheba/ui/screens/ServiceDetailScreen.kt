package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R

data class ServiceStep(val stepNumber: Int, val title: String)

// Data for New Voter Registration (National ID)
val voterRegistrationSteps = listOf(
    ServiceStep(1, "নতুন ভোটার হওয়ার জন্য https://services.nidw.gov.bd ঠিকানায় অনলাইনে আবেদন ফরম পূরণ করুন"),
    ServiceStep(2, "আবেদন প্রক্রিয়াটি যথাযথ সম্পন্ন হলে নির্দিষ্ট ফরমটি ডাউনলোড করে উভয় পাতায় প্রিন্ট করুন"),
    ServiceStep(3, "স্বাক্ষরিত ফরম-২/৩ এর শনাক্তকারীর ক্রমিক নম্বরধারী (পিতা-মাতা, ভাই-বোন) এর এনআইডি নম্বর ও স্বাক্ষর এবং যাচাইকারীর ক্রমিক সংশ্লিষ্ট जनप्रतिनिधिর এনআইডি নাম্বার ও স্বাক্ষর নিশ্চিত করুন"),
    ServiceStep(4, "বাদপড়া (যাদের জন্ম তারিখ ০১/০১/২০০৩ এর পূর্বে) ভোটারদের ক্ষেত্রে সংশ্লিষ্ট জনপ্রতিনিধি কর্তৃক ইতোপূর্বে কখন’ই ভোটার হননি মর্মে প্রত্যয়ন দাখিল করতে হবে"),
    ServiceStep(5, "শিক্ষাগত যোগ্যতার (এসএসসি/জেএসসি/পিইসি) সনদ (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(6, "পিতা-মাতার এনআইডি (পিতা-মাতা উভয়ে’ই মৃত হলে মৃত্যুর সনদ সংযুক্ত করুন)"),
    ServiceStep(7, "অনলাইন জন্ম নিবন্ধন সনদ"),
    ServiceStep(8, "পাসপোর্ট (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(9, "নাগরিকত্ব সনদ"),
    ServiceStep(10, "রক্তের গ্রুপ পরীক্ষার প্রতিবেদন"),
    ServiceStep(11, "স্বামী/স্ত্রীর এনআইডি কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(12, "বিদ্যুৎ বিলের কপি (নিজ/পিতা-মাতা নামীয়), অন্যান্য ক্ষেত্রে সংশ্লিষ্ট বাড়ির এনআইডি কপি ও প্রত্যয়নপত্র"),
    ServiceStep(13, "বিবাহের কাবিননামা (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(14, "যাদের জন্ম তারিখ: ০১/০১/২০০৪ বা তার পূর্বে শুধুমাত্র তারাই ভোটার নিবন্ধনের জন্য আবেদন দাখিল করতে পারবেন এবং সকল কাগজপত্র A4 সাইজের কাগজে সত্যায়িত করে জমা দিবেন")
)

// Data for Voter Transfer
val voterTransferSteps = listOf(
    ServiceStep(1, "ভোটার স্থানান্তরের জন্য https://services.nidw.gov.bd ঠিকানা থেকে ভোটার স্থানান্তর ফরম-১৩ ডাউনলোড করে উভয় পাতায় প্রিন্ট করুন"),
    ServiceStep(2, "স্বাক্ষরিত ফরম-১৩ এর ২য় পাতায় শনাক্তকারীর ক্রমিক সংশ্লিষ্ট जनप्रतिनिधिর এনআইডি নাম্বার ও স্বাক্ষর নিশ্চিত করুন"),
    ServiceStep(3, "সংশ্লিষ্ট ব্যক্তি নিজে স্ব-শরীরে উপস্থিত হয়ে উপজেলা নির্বাচন অফিসে (উপজেলা সার্ভার স্টেশনে) আবেদন জমা দিন"),
    ServiceStep(4, "ফরম-১৩ এর ক্রমিক ৩ এ মোবাইল ফোন নম্বরের কলামটি অবশ্যই পূরণ করতে হবে।"),
    ServiceStep(5, "সাথে..........................."),
    ServiceStep(6, "পিতা-মাতার এনআইডি কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(7, "ভাই-বোনের এনআইডি কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(8, "চাচা/ফুফু/দাদা-দাদীর এনআইডি কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(9, "স্বামী/স্ত্রীর এনআইডি কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(10, "জমির দলিল/পর্চা (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(11, "বিদ্যুৎ বিলের কপি (নিজ নামীয়), অন্যান্য ব্যক্তির নামে বিলের কপির ক্ষেত্রে সংশ্লিষ্ট ব্যক্তির এনআইডি’র কপি (সম্পর্ক উল্লেখসহ)"),
    ServiceStep(12, "বিয়ের কাবিননামা (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(13, "ট্রেড লাইসেন্সের রশিদ (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(14, "গ্যাস বিলের কপি (প্রযোজ্য ক্ষেত্রে)"),
    ServiceStep(15, "নাগরিকত্ব সনদ"),
    ServiceStep(16, "......................... সংযুক্ত করুন।")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(navController: NavController, serviceId: String?) {
    val (title, steps) = when (serviceId) {
        "nid" -> stringResource(id = R.string.service_nid_title) to voterRegistrationSteps
        "voter_transfer" -> stringResource(id = R.string.service_voter_transfer_title) to voterTransferSteps
        else -> "Service Details" to emptyList() // Fallback
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Steps Section
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    steps.forEach { step ->
                        StepRow(step = step)
                    }
                }
            }

            // Visual Aid Button
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(id = R.string.service_detail_visual_aid), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            }
        }
    }
}

@Composable
fun StepRow(step: ServiceStep) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${step.stepNumber}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LinkifiedText(
            text = step.title,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun LinkifiedText(text: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        val urlRegex = "(https?://\\S+)".toRegex()
        var lastIndex = 0

        urlRegex.findAll(text).forEach { matchResult ->
            val url = matchResult.value
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }

            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                append(url)
            }
            pop()
            lastIndex = endIndex
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier,
        onClick = {
            annotatedString.getStringAnnotations(tag = "URL", start = it, end = it)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        }
    )
}
