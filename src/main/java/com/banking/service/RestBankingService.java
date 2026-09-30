package com.banking.service;

import com.banking.dto.AccountResponse;
import com.banking.dto.BeneficiaryRequest;
import com.banking.dto.BeneficiaryResponse;
import com.banking.dto.CreateAccountRequest;
import com.banking.dto.DepositRequest;
import com.banking.dto.LoginRequest;
import com.banking.dto.RegisterRequest;
import com.banking.dto.TransactionResponse;
import com.banking.dto.TransferRequest;
import com.banking.dto.UserResponse;
import com.banking.dto.WithdrawRequest;
import com.banking.entity.AccountEntity;
import com.banking.entity.BeneficiaryEntity;
import com.banking.entity.TransactionEntity;
import com.banking.entity.UserEntity;
import com.banking.exception.BusinessRuleException;
import com.banking.exception.ConflictException;
import com.banking.exception.ForbiddenOperationException;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.BeneficiaryRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RestBankingService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public RestBankingService(UserRepository userRepository, AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              BeneficiaryRepository beneficiaryRepository,
                              PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered.");
        }
        UserEntity user = userRepository.save(new UserEntity(request.firstName().trim(), request.lastName().trim(),
                email, passwordEncoder.encode(request.password()), "CUSTOMER"));
        return UserResponse.from(user);
    }

    public UserResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        return me(authentication);
    }

    public UserResponse me(Authentication authentication) {
        return UserResponse.from(findUser(authentication.getName()));
    }

    @Transactional
    public AccountResponse createAccount(Authentication authentication, CreateAccountRequest request) {
        UserEntity user = currentUser(authentication);
        String number = request.accountNumber().trim();
        if (accountRepository.existsByAccountNumberIgnoreCase(number)) {
            throw new ConflictException("Account number is already in use.");
        }
        return AccountResponse.from(accountRepository.save(
                new AccountEntity(user, number, request.accountType().trim().toUpperCase())));
    }

    @Transactional
    public List<AccountResponse> myAccounts(Authentication authentication) {
        Integer userId = currentUser(authentication).getUserId();
        return accountRepository.findByUser_UserId(userId).stream().map(AccountResponse::from).toList();
    }

    @Transactional
    public AccountResponse getAccount(Authentication authentication, String accountNumber) {
        AccountEntity account = getAccountEntity(accountNumber);
        requireOwnerOrAdmin(authentication, account);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse deposit(Authentication authentication, String accountNumber, DepositRequest request) {
        AccountEntity account = ownedAccount(authentication, accountNumber);
        ensureActive(account);
        account.setBalance(account.getBalance().add(request.amount()));
        transactionRepository.save(new TransactionEntity(null, account, request.amount(), "DEPOSIT", "Cash deposit"));
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse withdraw(Authentication authentication, String accountNumber, WithdrawRequest request) {
        AccountEntity account = ownedAccount(authentication, accountNumber);
        ensureActive(account);
        ensureSufficientBalance(account, request.amount());
        account.setBalance(account.getBalance().subtract(request.amount()));
        transactionRepository.save(new TransactionEntity(account, null, request.amount(), "WITHDRAW", "Cash withdrawal"));
        return AccountResponse.from(account);
    }

    @Transactional
    public void transfer(Authentication authentication, TransferRequest request) {
        AccountEntity sender = ownedAccount(authentication, request.senderAccountNumber());
        AccountEntity receiver = getAccountEntity(request.receiverAccountNumber());
        if (sender.getAccountId().equals(receiver.getAccountId())) {
            throw new BusinessRuleException("Sender and receiver must be different accounts.");
        }
        ensureActive(sender);
        ensureActive(receiver);
        ensureSufficientBalance(sender, request.amount());
        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));
        transactionRepository.save(new TransactionEntity(sender, receiver, request.amount(), "TRANSFER",
                "Transfer to " + receiver.getAccountNumber()));
    }

    @Transactional
    public List<TransactionResponse> transactions(Authentication authentication, String accountNumber) {
        AccountEntity account = getAccountEntity(accountNumber);
        requireOwnerOrAdmin(authentication, account);
        return transactionRepository
                .findByFromAccount_AccountIdOrToAccount_AccountIdOrderByTransactionDateDesc(
                        account.getAccountId(), account.getAccountId())
                .stream().map(TransactionResponse::from).toList();
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(Authentication authentication, BeneficiaryRequest request) {
        UserEntity user = currentUser(authentication);
        String accountNumber = request.accountNumber().trim();
        if (!accountRepository.existsByAccountNumberIgnoreCase(accountNumber)) {
            throw new ResourceNotFoundException("Beneficiary account was not found.");
        }
        if (beneficiaryRepository.existsByUser_UserIdAndAccountNumberIgnoreCase(user.getUserId(), accountNumber)) {
            throw new ConflictException("Beneficiary already exists.");
        }
        return BeneficiaryResponse.from(beneficiaryRepository.save(new BeneficiaryEntity(user, request.name().trim(),
                accountNumber, request.bankName().trim())));
    }

    @Transactional
    public List<BeneficiaryResponse> myBeneficiaries(Authentication authentication) {
        return beneficiaryRepository.findByUser_UserId(currentUser(authentication).getUserId()).stream()
                .map(BeneficiaryResponse::from).toList();
    }

    @Transactional
    public void removeBeneficiary(Authentication authentication, Integer beneficiaryId) {
        beneficiaryRepository.deleteByUser_UserIdAndBeneficiaryId(currentUser(authentication).getUserId(), beneficiaryId);
    }

    @Transactional
    public List<UserResponse> allUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public List<AccountResponse> allAccounts() {
        return accountRepository.findAll().stream().map(AccountResponse::from).toList();
    }

    @Transactional
    public List<TransactionResponse> allTransactions() {
        return transactionRepository.findAll().stream().map(TransactionResponse::from).toList();
    }

    @Transactional
    public AccountResponse changeAccountStatus(String accountNumber, boolean frozen) {
        AccountEntity account = getAccountEntity(accountNumber);
        account.setStatus(frozen ? "FROZEN" : "ACTIVE");
        return AccountResponse.from(account);
    }

    private UserEntity currentUser(Authentication authentication) {
        return findUser(authentication.getName());
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found."));
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return accountRepository.findByAccountNumberIgnoreCase(accountNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account was not found."));
    }

    private AccountEntity ownedAccount(Authentication authentication, String accountNumber) {
        AccountEntity account = getAccountEntity(accountNumber);
        if (!account.getUser().getEmail().equalsIgnoreCase(authentication.getName())
                && !isAdmin(authentication)) {
            throw new ForbiddenOperationException("You cannot access this account.");
        }
        return account;
    }

    private void requireOwnerOrAdmin(Authentication authentication, AccountEntity account) {
        if (!account.getUser().getEmail().equalsIgnoreCase(authentication.getName()) && !isAdmin(authentication)) {
            throw new ForbiddenOperationException("You cannot access this resource.");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void ensureActive(AccountEntity account) {
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BusinessRuleException("Account " + account.getAccountNumber() + " is frozen.");
        }
    }

    private void ensureSufficientBalance(AccountEntity account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient balance.");
        }
    }
}