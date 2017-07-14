package org.csi.yucca.adminapi.service.impl;

import org.csi.yucca.adminapi.service.PublicComponentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PublicComponentServiceImpl implements PublicComponentService{

}