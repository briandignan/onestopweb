package models

case class Promotion( productTypeId: Long, message: String, gvoiceCred: Option[Credentials], gmailInfo: Option[PromotionMailInfo] )
