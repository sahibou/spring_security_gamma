package com.gammaray.demo_gamma.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gammaray.demo_gamma.model.ContratModele;

@RestController
public class ContratRestController
{
  @PostMapping("/contrat/{id}")
  public ContratModele getContrat(@PathVariable("id") Integer id){
      return ContratModele.builder().withId(id).build();    
  }
}
