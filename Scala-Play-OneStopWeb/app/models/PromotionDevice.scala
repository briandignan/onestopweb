package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class PromotionDevice( id: Option[Long], name: String )

object PromotionDevice {

	val simpleParser = 
		get[Option[Long]]( "PromotionDevices.PromotionDeviceID" ) ~
		get[String]( "PromotionDevices.Name" ) map {
			case id ~ name => PromotionDevice( id, name )
		}
	
	def options(): Seq[(String, String)] = { 
		DB.withConnection { implicit connection => 
			SQL(
				"""
					SELECT * FROM PromotionDevices 
					ORDER BY Name
				"""
			).as( PromotionDevice.simpleParser *).map( pd => ( pd.id.get.toString, pd.name ) )
		}
	}
}