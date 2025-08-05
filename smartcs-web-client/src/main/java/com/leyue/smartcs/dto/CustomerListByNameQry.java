package com.leyue.smartcs.dto;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerListByNameQry extends Query {
   private String name;
}
