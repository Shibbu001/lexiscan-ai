package com.lexiscan.backend.service;
import org.apache.pdfbox.Loader; import org.apache.pdfbox.text.PDFTextStripper; import org.springframework.stereotype.Service; import org.springframework.web.multipart.MultipartFile;
@Service public class PdfService{public String extract(MultipartFile f)throws Exception{try(var d=Loader.loadPDF(f.getBytes())){return new PDFTextStripper().getText(d).trim();}}}
