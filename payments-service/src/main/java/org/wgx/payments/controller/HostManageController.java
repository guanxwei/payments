package org.wgx.payments.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.material.impl.dao.MaterialDAO;
import org.wgx.payments.material.impl.dao.PrivilegeRecordDAO;
import org.wgx.payments.material.impl.meta.PrivilegeRecord;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.JsonObject;
import org.wgx.payments.utils.UserContext;

@RequestMapping(path = "/api/music/heimdallr/host")
@RestController
public class HostManageController {

    @Resource
    private PrivilegeRecordDAO privilegeRecordDAO ;

    @Resource
    private MaterialDAO materialDAO;

    @RequestMapping(path = "/add")
    public JsonObject add(
            @RequestParam(name = "host", required = true) final String host,
            @RequestParam(name = "name", required = true) final String name,
            @RequestParam(name = "type", defaultValue = "1") final int type) {
        if (privilegeRecordDAO.getByHostAndName(host, name) != null) {
            return JsonObject.start().code(400).msg("Duplicated Record!");
        }
        if (materialDAO.retriveByName(name) == null) {
            return JsonObject.start().code(400).msg("Material not exits!");
        }
        PrivilegeRecord record = PrivilegeRecord.builder()
                .host(host)
                .materialName(name)
                .user(UserContext.getUser())
                .type(type)
                .build();
        privilegeRecordDAO.save(record);
        return JsonObject.start().code(200);
    }

    @RequestMapping(path = "/list")
    public JsonObject list(@RequestParam(name = "name", required = true) final String name) {
        List<PrivilegeRecord> records = privilegeRecordDAO.getListByUser(UserContext.getUser());
        List<PrivilegeRecord> targets =  records.parallelStream().filter(record -> {
            return record.getMaterialName().equals(name);
        })
        .collect(Collectors.toList());
        return JsonObject.start().code(200).data(Jackson.json(targets));
    }
}
