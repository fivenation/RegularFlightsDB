import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

import org.jsoup._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.io.{BufferedWriter, File, FileWriter}

import scala.collection.mutable.ListBuffer

object main extends App {
  val page = "http://www.vnukovo.ru/flights/online-timetable/#tab-sortie";

  parser(page)

  def parser(_page: String) = {
    val doc = Jsoup.connect(_page).get()
    val table = doc.select("table[class=schedule-table-inner blue table-dflight-wrap]").select("tr")
    var i : Int = 0
    while (!table.eq(i).hasClass("visualyhidden")) {
      val columns = table.eq(i).select("td")
      val Array(time, date) = columns.eq(0).text().split(" ")
      val Array(day, mounth, year) = date.split('.')
      val Array(hours, minutes) = time.split(":")
      val flightNumber = columns.eq(1).text() // Flight number
      val airline = columns.eq(2).text() // Airline
      val destCity = columns.eq(4).text() // City
      val status = columns.eq(5).text() // Status

      val jsondoc =
        ("datetime" ->
          ("day" -> day.toInt) ~
          ("mounth" -> mounth.toInt) ~
          ("year" -> year.toInt) ~
          ("hours" -> hours.toInt) ~
          ("minutes" -> minutes.toInt)
        ) ~
        ("flightNumber" -> flightNumber) ~
        ("airline" -> airline) ~
        ("destCity" -> destCity) ~
        ("status" -> status)

      writeFile(compactRender(jsondoc), s"Row${(i+1).toString}")
      i += 1
    }
    println("Parsing completed!")
  }

  def writeFile(json: String, name: String): Unit = {
    val dir = new File("E:\\Programming\\IdeaProjects\\flightsdb\\JSON\\").mkdir()
    val file = new File("E:\\Programming\\IdeaProjects\\flightsdb\\JSON\\" + s"${name}" + ".json")
    val buffer = new BufferedWriter(new FileWriter(file))
    buffer.write(json)
    buffer.close()
  }

}

