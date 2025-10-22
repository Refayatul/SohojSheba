package com.bonfire.shohojsheba.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.models.Service

val citizenServices = listOf(
    Service(
        id = "nid",
        icon = Icons.Default.Badge,
        titleRes = R.string.service_nid_title,
        subtitleRes = R.string.service_nid_subtitle
    ),
    Service(
        id = "voter_transfer",
        icon = Icons.Default.SwapHoriz,
        titleRes = R.string.service_voter_transfer_title,
        subtitleRes = R.string.service_voter_transfer_subtitle
    ),
    Service(
        id = "passport",
        icon = Icons.Default.Book,
        titleRes = R.string.service_passport_title,
        subtitleRes = R.string.service_passport_subtitle
    ),
    Service(
        id = "birth_cert",
        icon = Icons.Default.WorkspacePremium,
        titleRes = R.string.service_birth_cert_title,
        subtitleRes = R.string.service_birth_cert_subtitle
    ),
    Service(
        id = "driving_license",
        icon = Icons.Default.DirectionsCar,
        titleRes = R.string.service_driving_license_title,
        subtitleRes = R.string.service_driving_license_subtitle
    ),
    Service(
        id = "land_reg",
        icon = Icons.Default.OtherHouses,
        titleRes = R.string.service_land_reg_title,
        subtitleRes = R.string.service_land_reg_subtitle
    ),
    Service(
        id = "marriage_reg",
        icon = Icons.Default.Favorite,
        titleRes = R.string.service_marriage_reg_title,
        subtitleRes = R.string.service_marriage_reg_subtitle
    )
)

val entrepreneurServices = listOf(
    Service(
        id = "trade_license",
        icon = Icons.Default.Business,
        titleRes = R.string.service_trade_license_title,
        subtitleRes = R.string.service_trade_license_subtitle
    ),
    Service(
        id = "company_reg",
        icon = Icons.Default.DomainAdd,
        titleRes = R.string.service_company_reg_title,
        subtitleRes = R.string.service_company_reg_subtitle
    ),
    Service(
        id = "tin_cert",
        icon = Icons.Default.Receipt,
        titleRes = R.string.service_tin_cert_title,
        subtitleRes = R.string.service_tin_cert_subtitle
    ),
    Service(
        id = "vat_reg",
        icon = Icons.Default.MonetizationOn,
        titleRes = R.string.service_vat_reg_title,
        subtitleRes = R.string.service_vat_reg_subtitle
    )
)

val farmerServices = listOf(
    Service(
        id = "agri_portal",
        icon = Icons.Default.Spa,
        titleRes = R.string.service_agri_portal_title,
        subtitleRes = R.string.service_agri_portal_subtitle
    ),
    Service(
        id = "fertilizer",
        icon = Icons.Default.Eco,
        titleRes = R.string.service_fertilizer_rec_title,
        subtitleRes = R.string.service_fertilizer_rec_subtitle
    ),
    Service(
        id = "agri_loan",
        icon = Icons.Default.Payments,
        titleRes = R.string.service_agri_loan_title,
        subtitleRes = R.string.service_agri_loan_subtitle
    ),
    Service(
        id = "seed_cert",
        icon = Icons.Default.Agriculture,
        titleRes = R.string.service_seed_cert_title,
        subtitleRes = R.string.service_seed_cert_subtitle
    )
)

val govtOfficeServices = listOf(
    Service(
        id = "power_div",
        icon = Icons.Default.OnlinePrediction,
        titleRes = R.string.service_power_div_title,
        subtitleRes = R.string.service_power_div_subtitle
    ),
    Service(
        id = "brta",
        icon = Icons.Default.CorporateFare,
        titleRes = R.string.service_brta_title,
        subtitleRes = R.string.service_brta_subtitle
    ),
    Service(
        id = "education_board",
        icon = Icons.Default.Functions,
        titleRes = R.string.service_education_board_title,
        subtitleRes = R.string.service_education_board_subtitle
    ),
    Service(
        id = "epg",
        icon = Icons.Default.Gavel,
        titleRes = R.string.service_public_procurement_title,
        subtitleRes = R.string.service_public_procurement_subtitle
    )
)

val allServices = citizenServices + entrepreneurServices + farmerServices + govtOfficeServices
