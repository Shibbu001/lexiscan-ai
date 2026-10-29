package com.lexiscan.backend.controller;
import com.fasterxml.jackson.databind.*;import com.lexiscan.backend.entity.Document;import com.lexiscan.backend.service.DocumentService;import org.springframework.http.MediaType;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import java.util.*;
@RestController @RequestMapping("/api/documents") public class DocumentController{
 private final DocumentService service;private final ObjectMapper mapper;public DocumentController(DocumentService s,ObjectMapper m){service=s;mapper=m;}
 @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)public Map<String,Object> upload(@RequestParam MultipartFile file,Authentication a)throws Exception{return out(service.upload(file,a.getName()));}
 @GetMapping public List<Map<String,Object>> list(Authentication a){return service.list(a.getName()).stream().map(this::out).toList();}
 @GetMapping("/{id}")public Map<String,Object> get(@PathVariable Long id,Authentication a){return out(service.get(id,a.getName()));}
 @DeleteMapping("/{id}")public Map<String,String> delete(@PathVariable Long id,Authentication a)throws Exception{service.delete(id,a.getName());return Map.of("message","Deleted");}
 private Map<String,Object> out(Document d){try{return Map.of("id",d.getId(),"fileName",d.getOriginalFileName(),"status",d.getStatus(),"fileSize",d.getFileSize(),"createdAt",d.getCreatedAt(),"analysis",d.getAnalysisJson()==null?mapper.createObjectNode():mapper.readTree(d.getAnalysisJson()));}catch(Exception e){throw new RuntimeException(e);}}
}
