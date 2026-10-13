package fu.se.chapter15api.controllers;

import fu.se.chapter15api.pojos.Orchid;
import fu.se.chapter15api.services.IOrchidService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orchids")
public class OrchidController {
    private final IOrchidService service;

    public OrchidController(IOrchidService service) {
        this.service = service;
    }

    // PDF uses /orchids/; the supplied README uses /orchids.
    @GetMapping({"", "/"})
    public ResponseEntity<List<Orchid>> fetchAll() {
        return ResponseEntity.ok(service.getAllOrchids());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orchid> getById(@PathVariable int id) {
        return service.getOrchidByID(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Orchid> saveOrchid(@Valid @RequestBody Orchid orchid) {
        Orchid created = service.insertOrchid(orchid);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orchid> updateOrchid(@PathVariable int id, @Valid @RequestBody Orchid orchid) {
        try {
            return ResponseEntity.ok(service.updateOrchid(id, orchid));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrchid(@PathVariable int id) {
        if (service.getOrchidByID(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteOrchid(id);
        return ResponseEntity.noContent().build();
    }
}
