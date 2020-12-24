package com.rison.es.utils;

import com.rison.es.main.Goods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HTMLParse {
    private static String url = "https://search.jd.com/Search?keyword=java&page=3&s=56&click=0";

    public static List<Goods> getGoodsList() throws IOException {
        Document document = (Document) Jsoup.connect(url).get();
        Elements elements = document.getElementsByClass("gl-i-wrap");
        List<Goods> goodsList = new ArrayList<>();
        for (Element e : elements){
            System.out.println("name:" + e.getElementsByClass("p-name").first().getElementsByTag("em").text());
            System.out.println("price:"+ e.getElementsByClass("p-price").text());
            System.out.println("img:" + e.getElementsByClass("p-img").first().getElementsByTag("img").attr("data-lazy-img"));
            System.out.println("=====================================================================================");
            Goods goods = new Goods();
            goods.setName(e.getElementsByClass("p-name").first().getElementsByTag("em").text());
            goods.setPrice(e.getElementsByClass("p-price").text());
            goods.setImg(e.getElementsByClass("p-img").first().getElementsByTag("img").attr("data-lazy-img"));
            goodsList.add(goods);
        }
        return goodsList;
    }

    public static void main(String[] args) throws IOException {

        getGoodsList();

    }





}
