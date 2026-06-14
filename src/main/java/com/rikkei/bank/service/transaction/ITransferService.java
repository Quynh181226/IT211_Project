package com.rikkei.bank.service.transaction;

import com.rikkei.bank.dto.transaction.request.TransferRequest;
import com.rikkei.bank.dto.transaction.response.TransferResponse;
import com.rikkei.bank.entity.User;

public interface ITransferService {

    TransferResponse transfer(TransferRequest request, User currentUser);
}