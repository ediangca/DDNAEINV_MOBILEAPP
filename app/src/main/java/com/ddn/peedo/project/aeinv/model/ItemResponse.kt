package com.ddn.peedo.project.aeinv.model

import java.util.Date
typealias ItemList = List<ItemResponse>

data class ItemResponse(
    val SourceTable: String,
    val ITEMID: Int,
    val REFNO: String,
    val Module: String,
    val TranferFlag: Boolean,
    val TransferRefNo: String?,
    val ReturnFlag: Boolean,
    val ReturnRefNo: String?,
    val postFlag: Boolean,
    val IID: String,
    val Brand: String?,
    val Model: String?,
    val Description: String,
    val SerialNo: String,
    val PropertyNo: String,
    val QRCode: String,
    val Unit: String,
    val Amount: Double,
    val Date_Acquired: String, // Can be converted to Date if needed
    val LGU: String,
    val FUND: String,
    val TransferType: String?,
    val TransferOthersType: String?,
    val TransferReason: String?,
    val ReturnType: String?,
    val ReturnOthersType: String?,
    val Issued: String?,
    val IssuedBy: String?,
    val IssuedByOffice: String?,
    val Received: String?,
    val ReceivedBy: String?,
    val ReceivedByOffice: String?,
    val Approved: String?,
    val ApprovedBy: String?,
    val ApprovedByOffice: String?,
    val Created: String?,
    val CreatedBy: String?,
    val CreatedByOffice: String?
)

