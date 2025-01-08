package com.interswitchng.smartpos.shared.models.core

enum class IswLocal(val currency: String, internal val code: String) {
    GHANA("\u20B5", "936"),
    NIGERIA("\u20A6", "566"),
    USA("\u0024", "840"),
    INDONESIA("\u0052\u0070", "360")
}

var CURRENCYTYPE = IswLocal.NIGERIA.currency
