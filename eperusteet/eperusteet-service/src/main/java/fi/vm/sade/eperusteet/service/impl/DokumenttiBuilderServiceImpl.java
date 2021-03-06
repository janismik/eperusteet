/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.Arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Termi;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.service.internal.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 *
 * @author jussi
 */
@Service
public class DokumenttiBuilderServiceImpl implements DokumenttiBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiBuilderServiceImpl.class);

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private TermistoRepository termistoRepository;

    @Override
    public String generateXML(Peruste peruste, Kieli kieli, Suoritustapakoodi suoritustapakoodi) throws
            TransformerConfigurationException,
            IOException,
            TransformerException,
            ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("book");
        rootElement.setAttribute("xmlns", "http://docbook.org/ns/docbook");
        rootElement.setAttribute("xmlns:h", "http://www.w3.org/1999/xhtml");
        doc.appendChild(rootElement);

        String nimi = getTextString(peruste.getNimi(), kieli);
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(nimi));
        rootElement.appendChild(titleElement);

        //rootElement.appendChild(doc.createElement("info"));
        // tähän vois lisätä esim jonkun info-elementin alkusivuja varten?

        for (Suoritustapa st : peruste.getSuoritustavat()) {
            if (st.getSuoritustapakoodi().equals(suoritustapakoodi)) {
                PerusteenOsaViite sisalto = peruste.getSuoritustapa(st.getSuoritustapakoodi()).getSisalto();
                addSisaltoElement(doc, peruste, rootElement, sisalto, 0, st, kieli);
            }
        }

        // pudotellaan tutkinnonosat paikalleen
        addTutkinnonosat(doc, peruste, kieli, suoritustapakoodi);

        // sanity check, ei feilata dokkariluontia, vaikka syntynyt dokkari
        // olisikin vähän pöljä
        if (rootElement.getChildNodes().getLength() <= 1) {
            Element fakeChapter = doc.createElement("chapter");
            fakeChapter.appendChild(doc.createElement("title"));
            rootElement.appendChild(fakeChapter);
        }

        addGlossary(doc, peruste, kieli);

        // helpottaa devaus-debugausta, voi olla vähän turha tuotannossa
        printDocument(doc, System.out);

        // rusikoidaan dokkarissa sekaisin olevat docbook- ja xhtml-osat
        // xsl-muunnoksena docbook-formaattiin
        DOMSource source = new DOMSource(doc);
        SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();

        File resultFile;
        try (InputStream xslresource = getClass().getClassLoader().getResourceAsStream("docgen/epdoc-markup.xsl")) {
            Templates templates = stf.newTemplates(new StreamSource(xslresource));
            // tai
            //Templates templates = stf.newTemplates(new StreamSource(new File("/full/path/to/epdoc-markup.xsl")));
            TransformerHandler th = stf.newTransformerHandler(templates);
            resultFile = File.createTempFile("peruste_" + UUID.randomUUID().toString(), ".xml");
            th.setResult(new StreamResult(resultFile));
            Transformer transformer = stf.newTransformer();
            transformer.transform(source, new SAXResult(th));
        }

        return resultFile.getAbsolutePath();
    }

    /**
     * Copies content from all the children of Jsoup Node on under W3C DOM Node
     * on given W3C document.
     *
     */
    private void jsoupIntoDOMNode(Document rootDoc, Node parentNode, org.jsoup.nodes.Node jsoupNode) {
        for (org.jsoup.nodes.Node child : jsoupNode.childNodes()) {
            createDOM(child, parentNode, rootDoc, new HashMap<String, String>());
        }
    }

    /**
     * The helper that copies content from the specified Jsoup Node into a W3C
     * Node.
     *
     * @param node The Jsoup node containing the content to copy to the
     * specified W3C Node.
     * @param out The W3C Node that receives the DOM content.
     */
    private void createDOM(org.jsoup.nodes.Node node, Node out, Document doc, Map<String, String> ns) {

        if (node instanceof org.jsoup.nodes.Document) {

            org.jsoup.nodes.Document d = ((org.jsoup.nodes.Document) node);
            for (org.jsoup.nodes.Node n : d.childNodes()) {
                createDOM(n, out, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.Element) {

            org.jsoup.nodes.Element e = ((org.jsoup.nodes.Element) node);
            // create all new elements into xhtml namespace
            org.w3c.dom.Element _e = doc.createElementNS("http://www.w3.org/1999/xhtml", e.tagName());
            out.appendChild(_e);
            org.jsoup.nodes.Attributes atts = e.attributes();

            for (org.jsoup.nodes.Attribute a : atts) {
                String attName = a.getKey();
                //omit xhtml namespace
                if (attName.equals("xmlns")) {
                    continue;
                }
                String attPrefix = getNSPrefix(attName);
                if (attPrefix != null) {
                    if (attPrefix.equals("xmlns")) {
                        ns.put(getLocalName(attName), a.getValue());
                    } else if (!attPrefix.equals("xml")) {
                        String namespace = ns.get(attPrefix);
                        if (namespace == null) {
                            //fix attribute names looking like qnames
                            attName = attName.replace(':', '_');
                        }
                    }
                }
                _e.setAttribute(attName, a.getValue());
            }

            for (org.jsoup.nodes.Node n : e.childNodes()) {
                createDOM(n, _e, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.TextNode) {

            org.jsoup.nodes.TextNode t = ((org.jsoup.nodes.TextNode) node);
            if (!(out instanceof Document)) {
                out.appendChild(doc.createTextNode(t.getWholeText()));
            }
        }
    }

    // some hacks for handling namespace in jsoup2DOM conversion
    private String getNSPrefix(String name) {
        if (name != null) {
            int pos = name.indexOf(':');
            if (pos > 0) {
                return name.substring(0, pos);
            }
        }
        return null;
    }

    private String getLocalName(String name) {
        if (name != null) {
            int pos = name.lastIndexOf(':');
            if (pos > 0) {
                return name.substring(pos + 1);
            }
        }
        return name;
    }

    private void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private void addTutkinnonMuodostuminen(Document doc, Element parentElement, Peruste peruste, int depth, Suoritustapa tapa, Kieli kieli) {

        // ew, dodgy trycatching
        try {
            parentElement.appendChild(
                    getTutkinnonMuodostuminenGeneric(
                            doc,
                            peruste,
                            // TODO: jostain muualta???
                            messages.translate("docgen.tutkinnon_muodostuminen.title", kieli),
                            depth,
                            tapa.getSuoritustapakoodi(),
                            kieli));
        } catch (Exception ex) {
            LOG.warn("adding getTutkinnonMuodostuminenGeneric failed miserably:", ex);
        }
    }

    private Element getTutkinnonMuodostuminenGeneric(
            Document doc, Peruste peruste, String title, int depth,
            Suoritustapakoodi suoritustapakoodi,
            Kieli kieli) {
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        RakenneModuuli rakenne = suoritustapa.getRakenne();

        Element returnElement;
        if (depth == 0) {
            returnElement = doc.createElement("chapter");
        } else {
            returnElement = doc.createElement("section");
        }

        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(title));
        returnElement.appendChild(titleElement);

        String kuvaus = getTextString(rakenne.getKuvaus(), kieli);
        if (StringUtils.isNotEmpty(kuvaus)) {
            Element para = doc.createElement("para");
            addMarkupToElement(doc, para, kuvaus);
            returnElement.appendChild(para);
        }

        addRakenneOsaRec(returnElement, rakenne, doc, kieli, 0);

        return returnElement;
    }


    private void addRakenneOsaRec(Element parent,
            AbstractRakenneOsa osa, Document doc, Kieli kieli, int depth) {

        if (osa instanceof RakenneModuuli) {
            RakenneModuuli rakenneModuuli = (RakenneModuuli) osa;

            String nimi = getTextString(rakenneModuuli.getNimi(), kieli);
            MuodostumisSaanto muodostumisSaanto = rakenneModuuli.getMuodostumisSaanto();
            String kokoTeksti = getKokoTeksti(muodostumisSaanto, kieli);
            String laajuusTeksti = getLaajuusTeksti(muodostumisSaanto, kieli);

            String kuvaus = getTextString(rakenneModuuli.getKuvaus(), kieli);

            // TODO: yksiköt, kappaleet
            if (kokoTeksti != null) {
                nimi += " | " + kokoTeksti;
            }
            if (laajuusTeksti != null) {
                nimi += " | " + laajuusTeksti;
            }

            Element element;
            if (depth == 0) {
                // root, kaikki liitetään parenttiin
                element = parent; //doc.createElement("informaltable");
                // TODO: jonkilainen hieno ylätason title?

            } else if (depth == 1) {
                // eka taso, table, tgroup ja body elementiks table
                Element table = doc.createElement("informaltable");
                parent.appendChild(table);

                // ensin koko taulukolle "otsikkorivi"
                Element tgroup = doc.createElement("tgroup");
                tgroup.setAttribute("cols", "1");
                table.appendChild(tgroup);
                Element tbody = doc.createElement("tbody");
                tgroup.appendChild(tbody);
                Element row = doc.createElement("row");
                ProcessingInstruction pi = doc.createProcessingInstruction("dbfo", null);
                pi.setData("bgcolor=\"#AAAAAA\"");
                row.appendChild(pi);
                tbody.appendChild(row);
                Element entry = doc.createElement("entry");
                entry.setAttribute("align", "center");
                Element emphasis = newBoldElement(doc, nimi.toUpperCase());
                entry.appendChild(emphasis);

                if (StringUtils.isNotEmpty(kuvaus)) {
                    Element para = doc.createElement("para");
                    para.appendChild(doc.createTextNode(kuvaus));
                    entry.appendChild(para);
                }

                row.appendChild(entry);
                element = table;

            } else if (depth == 2) {

                Element tgroup = doc.createElement("tgroup");
                tgroup.setAttribute("cols", "1");
                parent.appendChild(tgroup);

                Element tbody = doc.createElement("tbody");
                tgroup.appendChild(tbody);
                Element hrow = doc.createElement("row");
                ProcessingInstruction pi = doc.createProcessingInstruction("dbfo", null);
                pi.setData("bgcolor=\"#EEEEEE\"");
                hrow.appendChild(pi);
                tbody.appendChild(hrow);
                Element hentry = doc.createElement("entry");
                hentry.setAttribute("align", "center");
                Element emphasis = newBoldElement(doc, nimi);
                hentry.appendChild(emphasis);
                hrow.appendChild(hentry);

                if (StringUtils.isNotEmpty(kuvaus)) {
                    Element para = doc.createElement("para");
                    para.appendChild(doc.createTextNode(kuvaus));
                    hentry.appendChild(para);
                }

                element = tbody;

            } else if (depth == 3) {
                // toka taso row ja entry, elementiks entry
                Element row = doc.createElement("row");
                parent.appendChild(row);

                Element entry = doc.createElement("entry");
                Element emphasis = newBoldElement(doc, nimi);
                entry.appendChild(emphasis);
                row.appendChild(entry);

                if (StringUtils.isNotEmpty(kuvaus)) {
                    Element para = doc.createElement("para");
                    para.appendChild(doc.createTextNode(kuvaus));
                    entry.appendChild(para);
                }

                element = doc.createElement("itemizedlist");
                if (!rakenneModuuli.getOsat().isEmpty()) {
                    entry.appendChild(element);
                }
            } else {
                // moduulin nimi:
                Element nimiListItem = doc.createElement("listitem");
                Element emphasis = newBoldElement(doc, nimi);

                if (StringUtils.isNotEmpty(kuvaus)) {

                    Element sl = doc.createElement("simplelist");
                    Element mem1 = doc.createElement("member");
                    mem1.appendChild(emphasis);
                    Element mem2 = doc.createElement("member");
                    mem2.appendChild(doc.createTextNode(kuvaus));

                    sl.appendChild(mem1);
                    sl.appendChild(mem2);
                    nimiListItem.appendChild(sl);
                } else {
                    nimiListItem.appendChild(emphasis);
                }

                parent.appendChild(nimiListItem);

                element = doc.createElement("itemizedlist");

                if (!rakenneModuuli.getOsat().isEmpty()) {
                    nimiListItem.appendChild(element);
                }
            }

            for (AbstractRakenneOsa lapsi : rakenneModuuli.getOsat()) {
                addRakenneOsaRec(element, lapsi, doc, kieli, depth + 1);
            }

        } else if (osa instanceof RakenneOsa) {
            RakenneOsa rakenneOsa = (RakenneOsa) osa;

            BigDecimal laajuus = rakenneOsa.getTutkinnonOsaViite().getLaajuus();
            LaajuusYksikko laajuusYksikko = rakenneOsa.getTutkinnonOsaViite().getSuoritustapa().getLaajuusYksikko();
            String laajuusStr = "";
            String yks = "";
            if (laajuus != null) {
                laajuusStr = laajuus.stripTrailingZeros().toPlainString();
                if (laajuusYksikko == LaajuusYksikko.OSAAMISPISTE) {
                    yks = messages.translate("docgen.laajuus.osp", kieli);
                } else {
                    yks = messages.translate("docgen.laajuus.ov", kieli);
                }
            }
            String nimi = getTextString(rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getNimi(), kieli);
            String kuvaus = getTextString(rakenneOsa.getKuvaus(), kieli);

            String refid = "tutkinnonosa" + rakenneOsa.getTutkinnonOsaViite().getTutkinnonOsa().getId();
            Element xref = doc.createElement("xref");
            xref.setAttribute("linkend", refid);
            xref.setAttribute("xrefstyle", "select: labelnumber");
            Element linkElement = doc.createElement("link");
            linkElement.setAttribute("linkend", refid);

            StringBuilder nimiBldr = new StringBuilder();
            nimiBldr.append(" ").append(nimi);
            // TODO: jotain fiksumpaa kuin verrata "0.00":aan?
            if (!laajuusStr.isEmpty() && !laajuusStr.equals("0.00")) {
                nimiBldr.append(", ").append(laajuusStr).append(" ").append(yks);
            }

            linkElement.appendChild(doc.createTextNode(nimiBldr.toString()));

            if (rakenneOsa.isPakollinen()) {
                String glyph = messages.translate("docgen.rakenneosa.pakollinen.glyph", kieli);
                Element em = newBoldElement(doc, glyph);
                linkElement.appendChild(doc.createTextNode(", "));
                linkElement.appendChild(em);
            }

            // voi veljet...
            if (depth == 1) {
                // parent on tällä syvyydellä section, chapter tms.
                // tänne pitäisi joutua vain vähän oudossa tilanteessa, jossa
                // ylimmälle tasolle on pistetty tutkinnonosa ryhmä-elementin
                // sijasta
                // tehdään siis vain esim tylsästi para-elementti ja lihavoitu
                // teksti.
                Element para = doc.createElement("para");
                Element em = newBoldElement(doc);
                em.appendChild(xref);
                em.appendChild(linkElement);
                para.appendChild(em);
                parent.appendChild(para);
                para.appendChild(doc.createTextNode(kuvaus));
            } else if (depth == 2) {
                // parent on tällä syvyydellä aina informaltable
                Element tgroup = doc.createElement("tgroup");
                tgroup.setAttribute("cols", "1");
                Element tbody = doc.createElement("tbody");
                Element row = doc.createElement("row");
                Element entry = doc.createElement("entry");
                Element para = doc.createElement("para");
                para.appendChild(doc.createTextNode(kuvaus));

                parent.appendChild(tgroup);
                tgroup.appendChild(tbody);
                tbody.appendChild(row);
                row.appendChild(entry);
                entry.appendChild(xref);
                entry.appendChild(linkElement);
                entry.appendChild(para);
            } else if (depth == 3) {
                // parent on tällä syvyydellä aina tbody
                Element row = doc.createElement("row");
                Element entry = doc.createElement("entry");
                Element para = doc.createElement("para");
                para.appendChild(doc.createTextNode(kuvaus));

                parent.appendChild(row);
                row.appendChild(entry);
                entry.appendChild(xref);
                entry.appendChild(linkElement);
                entry.appendChild(para);
            } else if (depth >= 4) {
                // parent on tällä syvyydellä itemizedlist
                Element listitem = doc.createElement("listitem");
                parent.appendChild(listitem);

                if (StringUtils.isEmpty(kuvaus)) {
                    listitem.appendChild(xref);
                    listitem.appendChild(linkElement);
                } else {
                    Element slist = doc.createElement("simplelist");
                    Element mem1 = doc.createElement("member");
                    mem1.appendChild(xref);
                    mem1.appendChild(linkElement);
                    Element mem2 = doc.createElement("member");
                    mem2.appendChild(doc.createTextNode(kuvaus));
                    slist.appendChild(mem1);
                    slist.appendChild(mem2);
                    listitem.appendChild(slist);
                }

            } else {
                LOG.error("RakenneOsa tuntemattomalla syvyydellä {}", depth);
            }
        }
    }

    private void addSisaltoElement(Document doc, Peruste peruste, Element parentElement, PerusteenOsaViite sisalto, int depth, Suoritustapa tapa, Kieli kieli) {

        for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
            if (lapsi.getPerusteenOsa() == null) {
                continue;
            }

            PerusteenOsa po = lapsi.getPerusteenOsa();
            TekstiKappale tk = null;
            if (po instanceof TekstiKappale) {
                tk = (TekstiKappale) po;
            }

            if (tk == null) {
                LOG.error("*** eipä ole tekstikappale? " + po);
                continue;
            }

            Element element;
            if (depth == 0) {
                element = doc.createElement("chapter");
            } else {
                element = doc.createElement("section");
            }

            if (po.getTunniste() == PerusteenOsaTunniste.RAKENNE) {
                // poikkeustapauksena perusteen rakennepuun rendaus
                addTutkinnonMuodostuminen(doc, parentElement, peruste, depth, tapa, kieli);
            } else {
                // normikeississä sukelletaan syvemmälle puuhun
                String nimi = getTextString(tk.getNimi(), kieli);
                Element titleElement = doc.createElement("title");
                titleElement.appendChild(doc.createTextNode(nimi));
                String teksti = getTextString(tk.getTeksti(), kieli);

                addMarkupToElement(doc, element, teksti);

                element.appendChild(titleElement);

                addSisaltoElement(doc, peruste, element, lapsi, depth + 1, tapa, kieli); // keep it rollin

                parentElement.appendChild(element);
            }

        }
    }

    private String getLaajuusSuffiksi(final BigDecimal laajuus, final LaajuusYksikko yksikko, final Kieli kieli) {
        StringBuilder laajuusBuilder = new StringBuilder("");
        if (laajuus != null) {
            laajuusBuilder.append(", ");
            laajuusBuilder.append(laajuus.stripTrailingZeros().toPlainString());
            laajuusBuilder.append(" ");
            String yksikkoAvain;
            switch (yksikko) {
                case OPINTOVIIKKO:
                    yksikkoAvain = "docgen.laajuus.ov";
                    break;
                case OSAAMISPISTE:
                    yksikkoAvain = "docgen.laajuus.osp";
                    break;
                default:
                    throw new NotImplementedException("Tuntematon laajuusyksikko: " + yksikko);
            }
            laajuusBuilder.append(messages.translate(yksikkoAvain, kieli));
        }
        return laajuusBuilder.toString();
    }

    private String getOtsikko(final TutkinnonOsaViite viite, final Kieli kieli) {
        TutkinnonOsa osa = viite.getTutkinnonOsa();
        return getTextString(osa.getNimi(), kieli) +
                getLaajuusSuffiksi(viite.getLaajuus(), LaajuusYksikko.OSAAMISPISTE, kieli);
    }

    private void addTutkinnonosat(Document doc, Peruste peruste, final Kieli kieli, Suoritustapakoodi suoritustapakoodi) {

        Set<Suoritustapa> suoritustavat = peruste.getSuoritustavat();
        // tree setistä saadaan tutkinnonosaviitteet ulos halutussa,
        // comparatorin avulla järkättävässä järjestyksessä
        Set<TutkinnonOsaViite> osat = new TreeSet<>(
                new java.util.Comparator<TutkinnonOsaViite>() {
                    @Override
                    public int compare(TutkinnonOsaViite o1, TutkinnonOsaViite o2) {
                        String nimi1 = getTextString(o1.getTutkinnonOsa().getNimi(),kieli);
                        String nimi2 = getTextString(o2.getTutkinnonOsa().getNimi(),kieli);

                        // ensisijaisesti järjestysnumeron mukaan
                        int o1i = o1.getJarjestys() != null ? o1.getJarjestys() : Integer.MAX_VALUE;
                        int o2i = o2.getJarjestys() != null ? o2.getJarjestys() : Integer.MAX_VALUE;
                        if (o1i < o2i) {
                            return -1;
                        } else if (o1i > o2i) {
                            return 1;
                        }

                        // toissijaisesti aakkosjärjestyksessä
                        if (!nimi1.equals(nimi2)) {
                            return nimi1.compareTo(nimi2);
                        }

                        // viimekädessä kanta-avaimen mukaan
                        Long id1 = o1.getTutkinnonOsa().getId();
                        Long id2 = o2.getTutkinnonOsa().getId();
                        if (id1 < id2) {
                            return -1;
                        } else if (id1 > id2) {
                            return 1;
                        }

                        // On ne sitten samat
                        return 0;
                    }
                });

        for (Suoritustapa suoritustapa : suoritustavat) {
            if (suoritustapa.getSuoritustapakoodi().equals(suoritustapakoodi)) {
                for (TutkinnonOsaViite viite : suoritustapa.getTutkinnonOsat()) {
                    if (!viite.isPoistettu()) {
                        osat.add(viite);
                    }
                }
            }
        }

        for (TutkinnonOsaViite viite : osat) {
            TutkinnonOsa osa = viite.getTutkinnonOsa();

            Element element = doc.createElement("chapter");
            String refid = "tutkinnonosa" + osa.getId();
            element.setAttribute("id", refid);

            Element titleElement = doc.createElement("title");
            String osanOtsikko = getOtsikko(viite, kieli);
            titleElement.appendChild(doc.createTextNode(osanOtsikko));
            element.appendChild(titleElement);

            String kuvaus = getTextString(osa.getKuvaus(), kieli);
            if (StringUtils.isNotEmpty(kuvaus)) {
                Element para = doc.createElement("para");
                addMarkupToElement(doc, para, kuvaus);
                element.appendChild(para);
            }


            TutkinnonOsaTyyppi tyyppi = osa.getTyyppi();
            if (tyyppi == TutkinnonOsaTyyppi.NORMAALI) {
                addTavoitteet(doc, element, osa, kieli);
                addAmmattitaitovaatimukset(doc, element, osa, kieli);
                addAmmattitaidonOsoittamistavat(doc, element, osa, kieli);
                addArviointi(doc, element, osa.getArviointi(), tyyppi, kieli);
            } else if (tyyppi == TutkinnonOsaTyyppi.TUTKE2) {
                addTutke2Osat(doc, element, osa, kieli);
            }

            doc.getDocumentElement().appendChild(element);
        }
    }

    private void addTavoitteet(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String TavoitteetText = getTextString(tutkinnonOsa.getTavoitteet(), kieli);
        if (StringUtils.isEmpty(TavoitteetText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                TavoitteetText,
                messages.translate("docgen.tavoitteet.title", kieli));
    }

    private void addAmmattitaitovaatimukset(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String ammattitaitovaatimuksetText = getTextString(tutkinnonOsa.getAmmattitaitovaatimukset(), kieli);
        if (StringUtils.isEmpty(ammattitaitovaatimuksetText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                ammattitaitovaatimuksetText,
                messages.translate("docgen.ammattitaitovaatimukset.title", kieli));
    }

    private void addAmmattitaidonOsoittamistavat(Document doc, Element parent, TutkinnonOsa tutkinnonOsa, Kieli kieli) {

        String ammattitaidonOsoittamistavatText = getTextString(tutkinnonOsa.getAmmattitaidonOsoittamistavat(), kieli);
        if (StringUtils.isEmpty(ammattitaidonOsoittamistavatText)) {
            return;
        }

        addTekstiSectionGeneric(
                doc,
                parent,
                ammattitaidonOsoittamistavatText,
                messages.translate("docgen.ammattitaidon_osoittamistavat.title", kieli));
    }

    private void addTekstiSectionGeneric(Document doc, Element parent, String teksti, String title) {

        Element section = doc.createElement("section");
        Element sectionTitle = doc.createElement("title");
        sectionTitle.appendChild(doc.createTextNode(title));
        section.appendChild(sectionTitle);

        addMarkupToElement(doc, section, teksti);

        parent.appendChild(section);
    }

    private void addArviointi(Document doc, Element parent, Arviointi arviointi, TutkinnonOsaTyyppi tyyppi, Kieli kieli) {

        if (arviointi == null) {
            LOG.debug("Null arviointi, returning");
            return;
        }
        Element arviointiSection = doc.createElement("section");
        Element arviointiSectionTitle = doc.createElement("title");
        arviointiSectionTitle.appendChild(doc.createTextNode(
                messages.translate("docgen.arviointi.title", kieli)));
        arviointiSection.appendChild(arviointiSectionTitle);
        parent.appendChild(arviointiSection);

        TekstiPalanen lisatiedot = arviointi.getLisatiedot();
        if (lisatiedot != null) {
            Element lisatietoPara = doc.createElement("para");
            String lisatietoteksti = getTextString(lisatiedot, kieli);
            addMarkupToElement(doc, lisatietoPara, lisatietoteksti);
            arviointiSection.appendChild(lisatietoPara);
        } else {
            LOG.info("Lisatiedot was null");
        }

        List<ArvioinninKohdealue> arvioinninKohdealueet = sanitizeList(arviointi.getArvioinninKohdealueet());
        for (ArvioinninKohdealue ka : arvioinninKohdealueet) {
            if (ka.getArvioinninKohteet() == null) {
                LOG.warn("Arvioinninkohteet was null");
                continue;
            }
            if (ka.getArvioinninKohteet().isEmpty()) {
                LOG.warn("Arvioinninkohteet was empty");
                continue;
            }

            String otsikkoTeksti = tyyppi == TutkinnonOsaTyyppi.NORMAALI ?
                    getTextString(ka.getOtsikko(), kieli) :
                    messages.translate("docgen.tutke2.arvioinnin_kohteet.title", kieli);

            // Runnotaan jokainen kohde samaan taulukkoon, kukin kohde omalle
            // tgroup-ryhmänään ja niiden otsikko spännätyllä rivillä.
            // Arvioinnin kohteen otsikko taulukon alkuun omalla rivillään,
            // jotta dokumentin sektiointihierarkiasta ei tule liian syvä
            Element kaTable = doc.createElement("informaltable");
            arviointiSection.appendChild(kaTable);

            // Otsikkorivi arvioinninkohdealuetaulukkoon
            Element katgroup = doc.createElement("tgroup");
            katgroup.setAttribute("cols", "1");
            kaTable.appendChild(katgroup);
            Element kathead = doc.createElement("tbody");
            katgroup.appendChild(kathead);
            Element katheadRow = doc.createElement("row");
            kathead.appendChild(katheadRow);
            ProcessingInstruction katheadpi = doc.createProcessingInstruction("dbfo", null);
            katheadpi.setData("bgcolor=\"#AAAAAA\"");
            katheadRow.appendChild(katheadpi);
            Element katheadEntry = doc.createElement("entry");
            katheadEntry.setAttribute("align", "center");
            katheadRow.appendChild(katheadEntry);
            Element bold = newBoldElement(doc);
            bold.appendChild(doc.createTextNode(otsikkoTeksti.toUpperCase()));
            katheadEntry.appendChild(bold);

            // kukin arvioinninkohde omaan tgroupiinsa, kukin osaamistaso
            // omalla rivillään
            List<ArvioinninKohde> arvioinninKohteet = ka.getArvioinninKohteet();
            for (ArvioinninKohde kohde : arvioinninKohteet) {
                String kohdeTeksti = getTextString(kohde.getOtsikko(), kieli);

                Element groupElement = doc.createElement("tgroup");
                groupElement.setAttribute("cols", "2");
                Element colspecTaso = doc.createElement("colspec");
                colspecTaso.setAttribute("colname", "taso");
                colspecTaso.setAttribute("colwidth", "1*");
                groupElement.appendChild(colspecTaso);
                Element colspecKriteeri = doc.createElement("colspec");
                colspecKriteeri.setAttribute("colname", "kriteeri");
                colspecKriteeri.setAttribute("colwidth", "4*");
                groupElement.appendChild(colspecKriteeri);

                Element headerElement = doc.createElement("thead");
                Element headerRowElement = doc.createElement("row");

                Element headerEntry = doc.createElement("entry");
                headerEntry.appendChild(doc.createTextNode(kohdeTeksti));
                headerEntry.setAttribute("namest", "taso");
                headerEntry.setAttribute("nameend", "kriteeri");

                ProcessingInstruction pi = doc.createProcessingInstruction("dbfo", null);
                pi.setData("bgcolor=\"#EEEEEE\"");
                headerRowElement.appendChild(pi);

                headerRowElement.appendChild(headerEntry);

                headerElement.appendChild(headerRowElement);
                groupElement.appendChild(headerElement);

                Element bodyElement = doc.createElement("tbody");

                Set<OsaamistasonKriteeri> osaamistasonKriteerit = kohde.getOsaamistasonKriteerit();
                List<OsaamistasonKriteeri> kriteerilista = new ArrayList(osaamistasonKriteerit);
                java.util.Collections.sort(kriteerilista, new java.util.Comparator<OsaamistasonKriteeri>() {
                    @Override
                    public int compare(OsaamistasonKriteeri o1, OsaamistasonKriteeri o2) {
                        return (int) (o1.getOsaamistaso().getId() - o2.getOsaamistaso().getId());
                    }
                });

                for (OsaamistasonKriteeri krit : kriteerilista) {
                    String ktaso = getTextString(krit.getOsaamistaso().getOtsikko(), kieli);
                    List<String> kriteerit = asStringList(krit.getKriteerit(), kieli);

                    Element bodyRowElement = doc.createElement("row");
                    addTableCell(doc, bodyRowElement, ktaso);
                    addTableCell(doc, bodyRowElement, kriteerit);
                    bodyElement.appendChild(bodyRowElement);
                }

                // dirty fix: varmistetaan ettei feilata, jos osaamistason
                // kriteerejä ei ole (tbodyllä pitää olla row, jolla on entry)
                if (osaamistasonKriteerit.isEmpty()) {
                    Element bodyRowElement = doc.createElement("row");
                    addTableCell(doc, bodyRowElement, "");
                    bodyElement.appendChild(bodyRowElement);
                }

                groupElement.appendChild(bodyElement);
                kaTable.appendChild(groupElement);
            }
        }
    }

    private void addTableCell(Document doc, Element row, String text) {
        Element entry = doc.createElement("entry");
        entry.appendChild(doc.createTextNode(text));
        row.appendChild(entry);
    }

    private void addTableCell(Document doc, Element row, List<String> texts) {
        Element entry = doc.createElement("entry");
        for (String text : texts) {
            Element para = doc.createElement("para");
            para.appendChild(doc.createTextNode(text));
            entry.appendChild(para);
        }
        row.appendChild(entry);
    }

    private List<String> asStringList(List<TekstiPalanen> palaset, Kieli kieli) {
        List<String> list = new ArrayList<>();
        for (TekstiPalanen palanen : palaset) {
            list.add(getTextString(palanen, kieli));
        }
        return list;
    }

    private <T> List<T> sanitizeList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private String getTextString(TekstiPalanen teksti, Kieli kieli) {
        if (teksti == null
                || teksti.getTeksti() == null
                || teksti.getTeksti().get(kieli) == null) {
            return "";
        }
        return teksti.getTeksti().get(kieli);
    }

    private void addMarkupToElement(Document doc, Element element, String markup) {
        org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(markup);
        jsoupIntoDOMNode(doc, element, fragment.body());
    }

    private void addTutke2Osat(Document doc, Element element, TutkinnonOsa osa, Kieli kieli) {
        LOG.debug("> addTutke2Osat");
        List<OsaAlue> osaAlueet = osa.getOsaAlueet();

        for (OsaAlue osaAlue : osaAlueet) {
            String nimi = getTextString(osaAlue.getNimi(), kieli);
            Element sectionElement = doc.createElement("section");
            Element titleElement = doc.createElement("title");
            titleElement.appendChild(doc.createTextNode(nimi));
            sectionElement.appendChild(titleElement);
            element.appendChild(sectionElement);

            List<Osaamistavoite> osaamistavoitteet = osaAlue.getOsaamistavoitteet();

            // Parita pakollinen ja valinnainen osaamistavoite
            Map<Long, Pair<Osaamistavoite, Osaamistavoite>> tavoiteParit = new LinkedHashMap<>();
            for (Osaamistavoite tavoite : osaamistavoitteet) {
                Long key = tavoite.isPakollinen() ?
                        tavoite.getId() :
                        (tavoite.getEsitieto() != null ? tavoite.getEsitieto().getId() : tavoite.getId());

                if (tavoiteParit.containsKey(key)) {
                    Pair<Osaamistavoite, Osaamistavoite> pari = tavoiteParit.get(key);
                    pari = tavoite.isPakollinen() ?
                            Pair.of(tavoite, pari.getSecond()) :
                            Pair.of(pari.getFirst(), tavoite);
                    tavoiteParit.put(key, pari);
                } else {
                    Pair<Osaamistavoite, Osaamistavoite> pari = tavoite.isPakollinen() ?
                            Pair.of(tavoite, (Osaamistavoite)null) :
                            Pair.of((Osaamistavoite)null, tavoite);
                    tavoiteParit.put(key, pari);
                }
            }

            for (Pair<Osaamistavoite, Osaamistavoite> tavoitePari : tavoiteParit.values()) {
                Osaamistavoite pakollinen = tavoitePari.getFirst();
                Osaamistavoite valinnainen = tavoitePari.getSecond();

                Osaamistavoite otsikkoTavoite = pakollinen != null ? pakollinen : valinnainen;
                if (otsikkoTavoite == null) {
                    LOG.debug("pakollinen osaamistavoite == null && valinnainen osaamistavoite == null");
                    continue;
                }

                String tavoitteenNimi = getTextString(otsikkoTavoite.getNimi(), kieli);

                Element tavoiteSectionElement = doc.createElement("section");
                Element tavoiteTitleElement = doc.createElement("title");
                tavoiteTitleElement.appendChild(doc.createTextNode(tavoitteenNimi));
                tavoiteSectionElement.appendChild(tavoiteTitleElement);
                String refid = "tavoite_" + otsikkoTavoite.getId();
                tavoiteSectionElement.setAttribute("id", refid);
                sectionElement.appendChild(tavoiteSectionElement);

                Osaamistavoite[] tavoiteLista = new Osaamistavoite[] { pakollinen, valinnainen };
                for (Osaamistavoite tavoite : tavoiteLista) {
                    if (tavoite == null) {
                        continue;
                    }

                    String otsikkoAvain = tavoite.isPakollinen() ?
                            "docgen.tutke2.pakolliset_osaamistavoitteet.title" :
                            "docgen.tutke2.valinnaiset_osaamistavoitteet.title";
                    Element aliTavoiteSectionElement = doc.createElement("section");
                    Element aliTavoiteTitleElement = doc.createElement("title");
                    String otsikko = messages.translate(otsikkoAvain, kieli) +
                            getLaajuusSuffiksi(tavoite.getLaajuus(), LaajuusYksikko.OSAAMISPISTE, kieli);
                    aliTavoiteTitleElement.appendChild(doc.createTextNode(otsikko));
                    aliTavoiteSectionElement.appendChild(aliTavoiteTitleElement);
                    tavoiteSectionElement.appendChild(aliTavoiteSectionElement);

                    String tavoitteet = getTextString(tavoite.getTavoitteet(), kieli);
                    addMarkupToElement(doc, aliTavoiteSectionElement, tavoitteet);

                    Arviointi arviointi = tavoite.getArviointi();
                    addArviointi(doc, aliTavoiteSectionElement, arviointi, TutkinnonOsaTyyppi.TUTKE2, kieli);

                    TekstiPalanen tunnustaminen = tavoite.getTunnustaminen();
                    if (tunnustaminen != null) {
                        addTekstiSectionGeneric(
                                doc,
                                aliTavoiteSectionElement,
                                getTextString(tunnustaminen, kieli),
                                messages.translate("docgen.tutke2.tunnustaminen.title", kieli));
                    }
                }
            }
        }
    }

    private void addGlossary(Document doc, Peruste peruste, Kieli kieli) {
        Element appendix = doc.createElement("appendix");
        Element title = doc.createElement("title");
        title.appendChild(doc.createTextNode(messages.translate("docgen.termit.kasitteet-otsikko", kieli)));
        appendix.appendChild(title);
        Element glossary = doc.createElement("glossary");
        Element glossaryTitle = doc.createElement("title");
        glossaryTitle.appendChild(doc.createTextNode("")); // tyhjä
        glossary.appendChild(glossaryTitle);
        appendix.appendChild(glossary);

        List<Termi> termit = termistoRepository.findByPerusteId(peruste.getId());

        if (termit == null || termit.size() <= 0) {
            return;
        }
        doc.getDocumentElement().appendChild(appendix);

        for (Termi termi: termit) {
            String termiTermi = getTextString(termi.getTermi(), kieli);
            String termiSelitys = getTextString(termi.getSelitys(), kieli);

            Element glossEntry = doc.createElement("glossentry");
            glossary.appendChild(glossEntry);
            Element glossTerm = doc.createElement("glossterm");
            glossTerm.setAttribute("id", termi.getAvain());
            glossTerm.appendChild(doc.createTextNode(termiTermi));
            glossEntry.appendChild(glossTerm);

            Element glossDef = doc.createElement("glossdef");
            addMarkupToElement(doc, glossDef, termiSelitys);
            glossEntry.appendChild(glossDef);
        }
    }

    private String getKokoTeksti(MuodostumisSaanto saanto, Kieli kieli) {
        if (saanto == null || saanto.getKoko() == null) {
            return null;
        }

        MuodostumisSaanto.Koko koko = saanto.getKoko();
        Integer min = koko.getMinimi();
        Integer max = koko.getMaksimi();
        StringBuilder kokoBuilder = new StringBuilder("");
        if (min != null)  {
            kokoBuilder.append(min.toString());
        }
        if (min != null && max != null && !min.equals(max)) {
            kokoBuilder.append("-");
        }
        if (max != null && !max.equals(min)) {
            kokoBuilder.append(max.toString());
        }

        String yks = messages.translate("docgen.koko.kpl", kieli);
        kokoBuilder.append(" ");
        kokoBuilder.append(yks);
        return kokoBuilder.toString();
    }

    private String getLaajuusTeksti(MuodostumisSaanto saanto, Kieli kieli) {
        if (saanto == null || saanto.getLaajuus() == null) {
            return null;
        }

        MuodostumisSaanto.Laajuus laajuus = saanto.getLaajuus();
        Integer min = laajuus.getMinimi();
        Integer max = laajuus.getMaksimi();
        StringBuilder laajuusBuilder = new StringBuilder("");
        if (min != null)  {
            laajuusBuilder.append(min.toString());
        }
        if (min != null && max != null && !min.equals(max)) {
            laajuusBuilder.append("-");
        }
        if (max != null && !max.equals(min)) {
            laajuusBuilder.append(max.toString());
        }

        String yks = messages.translate("docgen.laajuus.ov", kieli);
        if (laajuus.getYksikko() == LaajuusYksikko.OSAAMISPISTE) {
            yks = messages.translate("docgen.laajuus.osp", kieli);
        }

        laajuusBuilder.append(" ");
        laajuusBuilder.append(yks);
        return laajuusBuilder.toString();
    }

    private Element newBoldElement(Document doc) {
        Element emphasis = doc.createElement("emphasis");
        emphasis.setAttribute("role", "strong");
        return emphasis;
    }

    private Element newBoldElement(Document doc, String teksti) {
        Element emphasis = newBoldElement(doc);
        emphasis.appendChild(doc.createTextNode(teksti));
        return emphasis;
    }
}
