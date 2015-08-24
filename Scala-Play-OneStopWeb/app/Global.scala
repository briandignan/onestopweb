import play.api._

import models._
import models.Item
import anorm._

object Global extends GlobalSettings {

	override def onStart( app: Application ) {
		InitialData.insert()
	}

}

object InitialData {

	def date( str: String ) = new java.text.SimpleDateFormat( "yyyy-MM-dd" ).parse( str )

	def insert() = {

		if ( User.findAll.isEmpty ) {

			Seq(
				User( None, "briandignan@gmail.com", "Brian", "Dignan", "password" ),
				User( None, "dosetareh@gmail.com", "Ariel", "Dosetareh", "password" ),
				User( None, "brianwei@terpmail.umd.edu", "Brian", "Weinblatt", "password" ),
				User( None, "ramaswamy.adithya@gmail.com", "Adithya", "Ramaswamy", "password" ),
				User( None, "eamare09@gmail.com", "Ebenezer", "Amare", "password" ),
				User( None, "jdephillipii@gmail.com", "Jim", "Dephillip", "password" ) )
			.foreach( User.create( _ ) )

		}

		/*
		if ( ProductType.findAll.isEmpty ) {
			Seq(
				ProductType( NotAssigned, "Drinks"),
				ProductType( NotAssigned, "Ice Cream"),
				ProductType( NotAssigned, "Frozen Foods"),
				ProductType( NotAssigned, "Tobacco"),
				ProductType( NotAssigned, "Pharmacy"),
				ProductType( NotAssigned, "Household"),
				ProductType( NotAssigned, "Confections"),
				ProductType( NotAssigned, "Snacks"),
				ProductType( NotAssigned, "School Supplies"),
				ProductType( NotAssigned, "Toiletries"),
				ProductType( NotAssigned, "Chips"),
				ProductType( NotAssigned, "Party Supplies"),
				ProductType( NotAssigned, "Misc"),
				ProductType( NotAssigned, "Lottery"))
			.foreach( ProductType.create( _ ) )
		}
		
		
		if ( Item.findAll.isEmpty ) {
			Seq(
				Item( NotAssigned, ProductType.findByName("Drinks").get.id.get, "028000515751", "Nesquik Strawberry Low Fat", new BigDecimal(new java.math.BigDecimal("2.19")), 0, 3, 3),
				Item( NotAssigned, ProductType.findByName("Ice Cream").get.id.get, "041000054143", "Good Humor Strawberry Shortcake", new BigDecimal(new java.math.BigDecimal("2.19")), 23, 10, 5)
			)
			.foreach( Item.create( _ ) )
		}
		
		
		if ( Vendor.findAll.isEmpty ) {
			Seq(
				Vendor(NotAssigned, "Berliner", Some("Hyattsville"), Some("Maryland"), Some("United States"), Some("eddie@berlinerfoods.com"), Some("240-417-7679 "), None, None),
				Vendor(NotAssigned, "C & C", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Canada Dry", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Costco", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "DMV", Some("Manassas"), Some("Virginia"), Some("United States"), Some("dmvdistributors1@gmail.com"), Some("703-231-6069 "), None, None),
				Vendor(NotAssigned, "Frtio Lay", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Giant", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Gourmet Innovations Inc.", Some("Fort Lauderdale"), Some("Florida"), Some("United States"), None, Some("877-502-7326 "), None, Some("954-972-9997")),
				Vendor(NotAssigned, "Inter Trade Corp.com", Some("Beltsville"), Some("Maryland"), Some("United States"), Some("info@intertradecorp.com"), Some("301-595-8999 "), Some("301-595-8998 "), None),
				Vendor(NotAssigned, "J Trading (Kim)", Some("College Park"), Some("Maryland"), Some("United States"), Some("hijskim@naver.com"), Some("703-462-3329 "), None, None),
				Vendor(NotAssigned, "J&L", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Kmart", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Lowes", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Maryland Lottery", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "N/a", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Q & A Services Inc", Some("Bethesda"), Some("Maryland"), Some("United States"), None, Some("202-569-7173 "), None, None),
				Vendor(NotAssigned, "Randy's", Some("Toledo"), Some("Ohio"), Some("United States"), None, Some("419-349-3226 "), None, None),
				Vendor(NotAssigned, "Red Bull/ Lift Off Distribution", Some("Savage"), Some("Maryland"), Some("USA"), None, Some("301-490-5542 "), None, Some("301-490-4715")),
				Vendor(NotAssigned, "Restaurant Depot", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "RJ Reynolds", Some("College Park"), Some("Maryland"), Some("United States"), Some("ngaruin@rjrt.com"), Some("800-974-2227 "), None, None),
				Vendor(NotAssigned, "Safeway", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Sam's Club", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Shoppers", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None),
				Vendor(NotAssigned, "Skeye", Some("Maumee"), Some("Ohio"), Some("United States"), None, Some("866-470-8806 "), None, None),
				Vendor(NotAssigned, "Triple C", Some("Baltimore"), Some("Maryland"), Some("USA"), None, Some("800-442-8742 "), None, None),
				Vendor(NotAssigned, "UTZ", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Walmart", None, None, None, None, None, None, None),
				Vendor(NotAssigned, "Wholesale Rolling Papers", Some("College Park"), Some("Maryland"), Some("United States"), None, None, None, None)
			)
			.foreach( Vendor.create( _ ) )
		}
		* 
		*/

	}

}