package org.wgx.payments.material.impl.service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.annotation.Resource;

import org.wgx.payments.material.HeimdallrService;
import org.wgx.payments.material.impl.dao.MaterialDAO;
import org.wgx.payments.material.impl.dao.PrivilegeRecordDAO;
import org.wgx.payments.material.impl.meta.Material;
import org.wgx.payments.material.impl.meta.PrivilegeRecord;
import org.wgx.payments.material.io.RetrieveMaterialRequest;
import org.wgx.payments.material.io.RetrieveMaterialResponse;
import org.wgx.payments.tools.Jackson;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC service to help retrieve material from Heimdallr service.
 * @author hzweiguanxiong
 *
 */
@Slf4j
public class RetrieveMaterialService implements HeimdallrService<RetrieveMaterialRequest, RetrieveMaterialResponse> {

    @Setter
    @Resource(name = "materialDAO")
    private MaterialDAO materialDAO;

    @Setter
    @Resource(name = "privilegeRecordDAO")
    private PrivilegeRecordDAO privilegeRecordDAO;

    @Override
    public RetrieveMaterialResponse execute(final RetrieveMaterialRequest request) {
        log.info("Receive request to retrive material [{}] for host [{}]", request.getName(), request.getHost());
        RetrieveMaterialResponse response = new RetrieveMaterialResponse();
        Material material = materialDAO.retriveByName(request.getName());
        log.info("Matetial [{}] found", Jackson.json(material));

        if (material == null) {
            log.info("Try to fetch unexisted material!");
            response.setCode(404);
            response.setMessage("Material not existed");
            return response;
        }
        PrivilegeRecord record = privilegeRecordDAO.getByHostAndName(request.getHost(), request.getName());
        if (record == null && request.getHostClass() != null) {
            record = privilegeRecordDAO.getByHostAndName(request.getHostClass(), request.getName());
        }
        if (record == null) {
            response.setCode(400);
            response.setMessage(String.format("Host is not authorized to retrieve material [%s]", request.getName()));
            return response;
        }
        decode(material);
        response.setCode(200);
        try {
            response.setMaterial(Jackson.json(material).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Fail to transter material data!");
        }
        return response;
    }

    private void decode(final Material material) {
        try {
            String encodedPub = material.getPublicKey();
            String encodedPri = material.getPrivateKey();
            String decodedPub = new String(Base64.getDecoder().decode(encodedPub.getBytes("UTF-8")), "UTF-8");
            String decodedPri = new String(Base64.getDecoder().decode(encodedPri.getBytes("UTF-8")), "UTF-8");
            String additional = new String(Base64.getDecoder().decode(material.getAdditional().getBytes("UTF-8")), "UTF-8");
            material.setPublicKey(decodedPub);
            material.setPrivateKey(decodedPri);
            material.setAdditional(additional);
        } catch (Exception e) {
            log.error("Fail to decrpt material data.", e);
        }
    }
}
