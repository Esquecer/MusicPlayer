package com.example.esquecer.myapplication.dao;

import com.example.esquecer.myapplication.bean.newsMessage;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class reptileMusicMessage {
    public ArrayList<newsMessage> getNewsFromNetEasy() {
        String url = "http://news.163.com/domestic/";
        ArrayList<newsMessage>arrayList = new ArrayList<>();
        try {
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
            Document doc = conn.get();
            String htmlTitle = doc.head().getElementsByTag("title").text().trim();
            Elements listDiv = doc.getElementsByAttributeValue("class", "today_news");
            for (Element element : listDiv) {
                Elements texts = element.getElementsByTag("a");
                for (Element text : texts) {
                    newsMessage msg = new newsMessage();
                    String ptitle = text.attr("title");
                    String ppath = text.attr("href");
                    Connection pconn = Jsoup.connect(ppath);
                    pconn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                    Document pdoc = pconn.get();
                    Elements newsS = pdoc.select("div.post_time_source");
                    String[] newsSplited = newsS.text().replace((char)(12288),' ').split("\\s+");
                    String ptime = newsSplited[0] + " "+ newsSplited[1];
                    String psource = newsSplited[3];
                    msg.setNewsSource(psource);
                    msg.setNewsTime(ptime);
                    msg.setNewsTitle(ptitle);
                    msg.setNewsPath(ppath);
                    arrayList.add(msg);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            return arrayList;
        }
    }
    public  ArrayList<newsMessage> getNewsSouHu(){
        String url = "http://news.sohu.com/";//搜狐
        ArrayList<newsMessage>arrayList = new ArrayList<>();
        try {
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
            Document doc = conn.get();
            Elements listDiv = doc.getElementsByAttributeValue("target","_blank");
            for (Element element : listDiv) {
                newsMessage msg = new newsMessage();
                String isVaildTitle = element.attr("title");
                if("".equals(isVaildTitle))continue;
                String ppath = element.attr("href");
                String ptitle = element.text();
                if("".equals(ppath) || "".equals(ptitle) || "javascript:void(0)".equals(ppath))continue;
                if(ppath.charAt(0)=='/' && ppath.charAt(1) == '/')ppath = "http:"+ppath;//有些网址没带http头
                if(ppath.charAt(7)=='t'||ppath.charAt(7)=='s')continue;//删掉" http://tv.sohu.com/...."和"http://sports.sohu.com/..."的网址
                ptitle = convertUnicode(ptitle);
                Connection pconn = Jsoup.connect(ppath);
                pconn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                Document pdoc =  pconn.get();
                Elements newsTime = pdoc.select("span.time");
                String ptime = newsTime.text();
                Elements newsSource= pdoc.getElementsByAttributeValue("data-role","original-link");
                String psource = newsSource.text();
                if("".equals(psource))continue;

                msg.setNewsSource(psource);
                msg.setNewsTime(ptime);
                msg.setNewsTitle(ptitle);
                msg.setNewsPath(ppath);
                arrayList.add(msg);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            return arrayList;
        }
    }
    public  ArrayList<newsMessage> getNewsFromSearch(String searchText) {
        String url = "http://news.baidu.com/ns?word="+searchText+"&tn=newstitle&from=news&cl=2&rn=20&ct=0";
        ArrayList<newsMessage>arrayList = new ArrayList<newsMessage>();

        String []ppath = new String[100];
        String []ptitle = new String[100];
        String []ptime = new String[100];
        String []psource = new String[100];
        int k= 0,p = 0;
        // TODO Auto-generated method stub
        try{
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
            Document doc = conn.get();
            Elements listDiv = doc.getElementsByAttributeValue("class", "result title");
            for(Element element1: listDiv){
                Elements element2 = element1.getElementsByAttributeValue("target", "_blank");
                for(Element element3:element2){
                    ppath[k] = element3.attr("href");
                    ptitle[k++] = element3.text();
                    //System.out.println(element3.text());
                }
                Elements element4 = element1.getElementsByAttributeValue("class", "c-title-author");
                for(Element element3:element4){
                    String []pltime = element3.text().split("\\s+");
                    psource[p] = pltime[0].substring(0, pltime[0].length());
                    ptime[p++] = pltime[1].substring(0, pltime[1].length());
                }
            }
            for(int i = 0 ; i < p ;i++){
                newsMessage msg = new newsMessage();
                msg.setNewsSource(psource[i]);
                msg.setNewsTime(ptime[i]);
                msg.setNewsTitle(ptitle[i]);
                msg.setNewsPath(ppath[i]);
                arrayList.add(msg);
            }
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        finally {
            return arrayList;
        }
    }
    public  String convertUnicode(String ori){//字符串中的Unicode转String，用于处理网页中的Unicode
        char aChar;
        int len = ori.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = ori.charAt(x++);
            if (aChar == '\\') {
                aChar = ori.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = ori.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);

        }
        return outBuffer.toString();
    }
}