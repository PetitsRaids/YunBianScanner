package com.raids.yunbianscanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
class History {

    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
    var barcode: String? = null
    var goodsName: String? = null
    var goodsPrice = 0.0
    var describe: String? = null

    override fun toString(): String {
        return "History(id=$id, barcode=$barcode, goodsName=$goodsName, goodsPrice=$goodsPrice, describe=$describe)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as History

        if (id != other.id) return false
        if (barcode != other.barcode) return false
        if (goodsName != other.goodsName) return false
        if (goodsPrice != other.goodsPrice) return false
        if (describe != other.describe) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (barcode?.hashCode() ?: 0)
        result = 31 * result + (goodsName?.hashCode() ?: 0)
        result = 31 * result + goodsPrice.hashCode()
        result = 31 * result + (describe?.hashCode() ?: 0)
        return result
    }


}