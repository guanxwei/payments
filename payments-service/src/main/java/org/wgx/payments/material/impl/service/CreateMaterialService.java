package org.wgx.payments.material.impl.service;

import java.util.Base64;

import javax.annotation.Resource;

import org.wgx.payments.material.HeimdallrService;
import org.wgx.payments.material.impl.dao.MaterialDAO;
import org.wgx.payments.material.impl.meta.Material;
import org.wgx.payments.material.io.CreateMaterialRequest;
import org.wgx.payments.material.io.CreateMaterialResponse;
import org.wgx.payments.tools.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * RPC service to create new material.
 * @author hzweiguanxiong
 *
 */
@Slf4j
public class CreateMaterialService implements HeimdallrService<CreateMaterialRequest, CreateMaterialResponse> {

    private static final ThreadLocal<String> MESSAGE = new ThreadLocal<>();

    @Resource
    private MaterialDAO materialDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateMaterialResponse execute(final CreateMaterialRequest request) {
        log.info("Receive request to create material with data [{}]", Jackson.json(request));
        CreateMaterialResponse response = new CreateMaterialResponse();
        if (!validate(request)) {
            response.setCode(400);
            response.setMessage(MESSAGE.get());
            return response;
        }
        if (materialDAO.retriveByName(request.getName()) != null) {
            response.setCode(400);
            response.setMessage("Material name has been used! Please use others");
            return response;
        }
        Material material = Material.builder()
                .name(request.getName())
                .privateKey(request.getPrivateKey())
                .publicKey(request.getPublicKey())
                .user(request.getUser())
                .build();
        encrypt(material);
        materialDAO.save(material);
        response.setCode(200);
        return response;
    }

    /**
     * @param material
     */
    private void encrypt(final Material material) {
        try {
            if (material.getPrivateKey() != null) {
                material.setPrivateKey(Base64.getEncoder().encodeToString(material.getPrivateKey().getBytes("UTF-8")));
            }
            if (material.getPublicKey() != null) {
                material.setPublicKey(Base64.getEncoder().encodeToString(material.getPublicKey().getBytes("UTF-8")));
            }
            if (material.getAdditional() != null) {
                material.setAdditional(Base64.getEncoder().encodeToString(material.getAdditional().getBytes("UTF-8")));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean validate(final CreateMaterialRequest request) {
        if (request.getName().length() > 255) {
            MESSAGE.set("Material name is too long");
            return false;
        }
        if (request.getPrivateKey().length() > 2048 || request.getPublicKey().length() > 2048) {
            MESSAGE.set("Private or public key is too long, we don't support them yet!");
            return false;
        }
        return true;
    }
}
