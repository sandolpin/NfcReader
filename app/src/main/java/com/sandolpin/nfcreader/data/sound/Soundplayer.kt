package com.sandolpin.nfcreader.data.sound

/** 効果音の種類。koutuu.mp3 / credit.mp3 / tag.mp3 に対応する。 */
enum class SoundType {
    TRANSIT, // 交通系IC(koutuu)
    CREDIT,  // クレジットカードなどISO-DEP(credit)
    TAG      // NDEFタグなどその他(tag)
}

/** 効果音の再生。ViewModel から使い、テストでは差し替えられるようにインターフェースにしてある。 */
interface SoundPlayer {
    fun play(type: SoundType)
    fun release()
}