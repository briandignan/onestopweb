package models

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = {
  	println( "page: " + page + " offset: " + offset + " items.size: " + items.size + " total: " + total )
  	Option(page + 1).filter(_ => (offset + items.size) < total)
  }
}