package com.dark.dss.service;

import com.dark.dss.entity.Client;
import com.dark.dss.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Listar todos
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    // Buscar por ID
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    // Guardar (Crear)
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    // Actualizar
    public Client update(Long id, Client clientDetails) {
        Client client = findById(id);
        client.setName(clientDetails.getName());
        client.setEmail(clientDetails.getEmail());
        client.setPhone(clientDetails.getPhone());

        return clientRepository.save(client);
    }

    // Eliminar
    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}