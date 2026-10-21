package com.sandolpin.nfcreader.domain.model

/** NFCタグの種類。 */
enum class TagType {
    FELICA,
    ISO_DEP,
    NDEF,
    OTHER;

    companion object {
        private const val TECH_NFC_F = "android.nfc.tech.NfcF"
        private const val TECH_ISO_DEP = "android.nfc.tech.IsoDep"
        private const val TECH_NDEF = "android.nfc.tech.Ndef"
        private const val TECH_NDEF_FORMATABLE = "android.nfc.tech.NdefFormatable"

        /** `Tag.techList` の内容から種類を決める。優先順位は FeliCa > ISO-DEP > NDEF > その他。 */
        fun fromTechList(techList: List<String>): TagType = when {
            TECH_NFC_F in techList -> FELICA
            TECH_ISO_DEP in techList -> ISO_DEP
            TECH_NDEF in techList || TECH_NDEF_FORMATABLE in techList -> NDEF
            else -> OTHER
        }
    }
}