package com.wsu.workorderproservice.service;

import com.wsu.workorderproservice.exception.DatabaseErrorException;
import com.wsu.workorderproservice.exception.InvalidRequestException;
import com.wsu.workorderproservice.model.WorkOrder;
import com.wsu.workorderproservice.model.WorkOrderLineItem;
import com.wsu.workorderproservice.repository.WorkOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    /**
     * This method used for retrieve the WorkOrder details by workOrderNumber
     * @param workOrderNumber - it's used to get WorkOrder details
     * @return - WorkOrder details.
     */
    //TODO: Convert WorkOrder entity model class to WorkOrderDTO and return back
    public WorkOrder get(Integer workOrderNumber) {
        Optional<WorkOrder> workOrder = workOrderRepository.findById(workOrderNumber);
        if (workOrder.isEmpty()) {
            throw new InvalidRequestException("Invalid WorkOrder number");
        }
        try {
            return workOrderRepository.findById(workOrderNumber).orElse(null);
        } catch (Exception e) {
            log.error("Failed to retrieve WorkOrder details. workOrderNumber:{}, Exception:{}", workOrderNumber, e);
            throw new DatabaseErrorException("Failed to retrieve WorkOrder details.", e);
        }
    }

    /**
     * This method used for add new WorkOrder with line items if any
     * @param workOrder - WorkOrder entity model class.
     * @return - returns saved WorkOrder entity model class
     */
    //TODO: Convert passed WorkOrderDTO to WorkOrder entity model class and once it's saved into the database then convert back to DTO and return back
    @Transactional(rollbackOn = Exception.class)
    public WorkOrder add(WorkOrder workOrder) {
        try {
            Set<WorkOrderLineItem> lineItems = workOrder.getLineItems();
            workOrder.setLineItems(null);
            WorkOrder workOrderResp = workOrderRepository.save(workOrder);
            lineItems.forEach(lineItem -> lineItem.setWorkOrderNumber(workOrderResp.getWorkOrderNumber()));
            workOrderResp.setLineItems(lineItems);
            return workOrderRepository.save(workOrderResp);
        } catch (Exception e) {
            log.error("Failed to add WorkOrder. Exception: ", e);
            throw new DatabaseErrorException("Failed to add new WorkOrder.", e);
        }
    }

    /**
     * This method used for update the existing WorkOrder based on given WorkOrder number
     * @param workOrderNumber - primary key that's used for update existing WorkOrder
     * @param workOrder - payload that contains updated WorkOrder
     * @return - Updated WorkOrder
     */
    
    //TODO: Convert passed WorkOrderDTO to WorkOrder entity model class and once it's updated into the database then convert back to DTO and return back
    @Transactional(rollbackOn = Exception.class)
    public WorkOrder update(Integer workOrderNumber, WorkOrder workOrder) {
        Optional<WorkOrder> workOrderResp = workOrderRepository.findById(workOrderNumber);
        if (workOrderResp.isEmpty()) {
            throw new InvalidRequestException("Invalid WorkOrder number");
        }
        try {
            workOrder.setWorkOrderNumber(workOrderResp.get().getWorkOrderNumber());
            workOrder.setDateTimeLastUpdated(new Date());
            if (!CollectionUtils.isEmpty(workOrder.getLineItems())) {
                workOrder.getLineItems().forEach(lineItem -> lineItem.setWorkOrderNumber(workOrder.getWorkOrderNumber()));
            }
            return workOrderRepository.save(workOrder);
        } catch (Exception e) {
            log.error("Failed to update WorkOrder. workOrderNumber:{}, Exception:{}", workOrderNumber, e);
            throw new DatabaseErrorException("Failed to update WorkOrder.", e);
        }
    }
}
