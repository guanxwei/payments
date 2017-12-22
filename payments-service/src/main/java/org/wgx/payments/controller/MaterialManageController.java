package org.wgx.payments.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.material.HeimdallrService;
import org.wgx.payments.material.impl.dao.MaterialDAO;
import org.wgx.payments.material.impl.meta.Material;
import org.wgx.payments.material.io.CreateMaterialRequest;
import org.wgx.payments.material.io.CreateMaterialResponse;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.JsonObject;
import org.wgx.payments.utils.UserContext;

import lombok.extern.slf4j.Slf4j;

@RequestMapping(path = "/api/music/heimdallr/material")
@RestController
@Slf4j
public class MaterialManageController {

    @Resource(name = "heimdallrCreateMaterialService")
    private HeimdallrService<CreateMaterialRequest, CreateMaterialResponse> heimdallrCreateMaterialService;

    @Resource
    private MaterialDAO materialDAO;

    @RequestMapping(path = "/add")
    public JsonObject add(@RequestParam(name = "name") final String name,
            @RequestParam(name = "privateKey") final String privateKey,
            @RequestParam(name = "publicKey") final String publicKey,
            @RequestParam(name = "additional") final String additional) {

        Material material = Material.builder()
                .name(name)
                .additional(additional)
                .privateKey(privateKey)
                .publicKey(publicKey)
                .build();
        log.info("Receive request to create new material with data [{}]", Jackson.json(material));
        CreateMaterialRequest request = new CreateMaterialRequest();
        request.setName(material.getName());
        request.setPrivateKey(material.getPrivateKey());
        request.setPublicKey(material.getPublicKey());
        request.setAdditional(material.getAdditional());
        request.setUser(UserContext.getUser());
        CreateMaterialResponse response = heimdallrCreateMaterialService.execute(request);
        return JsonObject.start().code(response.getCode()).msg(response.getMessage());
    }

    @RequestMapping(path = "/delete")
    public JsonObject delete(@RequestParam(name = "name", required = true) final String name) {
        log.info("Receive request to delete material [{}] for user [{}]", name, UserContext.getUser());
        materialDAO.delete(name);
        return JsonObject.start().code(200);
    }

    @RequestMapping(path = "/list")
    public JsonObject list() {
        List<Material> materials = materialDAO.getListByUser(UserContext.getUser());
        materials.forEach(material -> {
            material.setPrivateKey(null);
            material.setPublicKey(null);
            material.setAdditional(null);
        });
        return JsonObject.start().code(200).data(Jackson.json(materials));
    }
}
