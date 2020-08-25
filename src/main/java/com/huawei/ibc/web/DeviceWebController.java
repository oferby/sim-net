package com.huawei.ibc.web;

import com.huawei.ibc.model.controller.DatabaseControllerImpl;
import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.web.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

@RestController
public class DeviceWebController {

    @Autowired
    private DatabaseControllerImpl databaseController;

    @GetMapping("/device")
    public Collection<Device> getAllDevices(){
        Collection<AbstractDevice> allDevices = databaseController.getAllDevices();
        Collection<Device>devices=new ArrayList<>();
        for (AbstractDevice abstractDevice : allDevices) {
            devices.add(Device.getInstance(abstractDevice));
        }

        return devices;

    }




}
