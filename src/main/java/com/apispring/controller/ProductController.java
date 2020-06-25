package com.apispring.controller;

import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import com.apispring.models.Product;
import com.apispring.repository.ProductRepository;

@RestController
@RequestMapping("/api")
@Api(value = "Api Rest")
@CrossOrigin(origins = "*")
public class ProductController {

  @Autowired
  private ProductRepository productRepository;

  @ApiOperation(value = "Este metodo salva um produto")
  @PostMapping("/save")
  public ResponseEntity<Product> create(@RequestBody final Product product) {
    productRepository.save(product);
    return new ResponseEntity<>(product, HttpStatus.OK);
  }

  @ApiOperation(value = "Este metodo retorna todos os produtos")
  @GetMapping("/")
  public ResponseEntity<List<Product>> findAll() {
    List<Product> products = new ArrayList<>();
    products = productRepository.findAll();
    return new ResponseEntity<>(products, HttpStatus.OK);
  }

  @ApiOperation(value = "Este metodo retorna um produto")
  @GetMapping(path = "/{id}")
  public ResponseEntity<Optional<Product>> findById(@PathVariable Long id) {
    Optional<Product> product;
    try {
      product = productRepository.findById(id);
      if (product.isPresent()) {
        return new ResponseEntity<Optional<Product>>(product, HttpStatus.OK);

      }
      return new ResponseEntity<Optional<Product>>(HttpStatus.NOT_FOUND);

    } catch (EmptyResultDataAccessException e) {
      return new ResponseEntity<Optional<Product>>(HttpStatus.BAD_REQUEST);
    }
  }

  @ApiOperation(value = "Este metodo deleta um produto")
  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Optional<Product>> deleteById(@PathVariable Long id) {
    try {
      productRepository.deleteById(id);
      return new ResponseEntity<Optional<Product>>(HttpStatus.OK);
    } catch (EmptyResultDataAccessException e) {
      return new ResponseEntity<Optional<Product>>(HttpStatus.BAD_REQUEST);
    }
  }

  @ApiOperation(value = "Este metodo edita um produto")
  @PutMapping(value = "/{id}")
  public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product newProduct) {
    return productRepository.findById(id).map(product -> {
      product.setName(newProduct.getName());
      product.setDescription(newProduct.getDescription());
      product.setValue(newProduct.getValue());
      Product productUpdate = productRepository.save(product);
      return ResponseEntity.ok().body(productUpdate);
    }).orElse(ResponseEntity.notFound().build());
  }

  @ApiOperation(value = "Este metodo salva imagem")
  @PostMapping("/upload")
  public ResponseEntity<String> singleFileUpload(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) {
    StringBuilder result = new StringBuilder();
    String name = "";
    if (file.isEmpty()) {
      redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
      return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

    }
    try {
      byte[] nonce = new byte[12];
      new SecureRandom().nextBytes(nonce);
      for (byte temp : nonce) {
        result.append(String.format("%02x", temp));
      }
      String type = file.getContentType().replace("image/", ".");
      name = result.toString() + type;
      byte[] bytes = file.getBytes();
      String pathImage = System.getProperty("user.dir") + "/images/";
      Path path = Paths.get(pathImage + name);
      Files.write(path, bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<String>(name, HttpStatus.OK);

  }

  @Bean
  public BasicDataSource dataSource() throws URISyntaxException {
    URI dbUri = new URI(System.getenv("DATABASE_URL"));

    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
        + "?sslmode=require";

    BasicDataSource basicDataSource = new BasicDataSource();
    basicDataSource.setUrl(dbUrl);
    basicDataSource.setUsername(username);
    basicDataSource.setPassword(password);

    return basicDataSource;
  }
}