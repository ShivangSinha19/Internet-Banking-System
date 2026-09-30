package com.banking;

import com.banking.dto.CreateAccountRequest;
import com.banking.dto.DepositRequest;
import com.banking.dto.RegisterRequest;
import com.banking.dto.TransferRequest;
import com.banking.dto.WithdrawRequest;
import com.banking.entity.AccountEntity;
import com.banking.entity.UserEntity;
import com.banking.exception.BusinessRuleException;
import com.banking.exception.ConflictException;
import com.banking.exception.ForbiddenOperationException;
import com.banking.repository.AccountRepository;
import com.banking.repository.BeneficiaryRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import com.banking.service.RestBankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestBankingServiceTest {
	private UserRepository users;
	private AccountRepository accounts;
	private TransactionRepository transactions;
	private Authentication authentication;
	private RestBankingService service;
	private UserEntity user;
	private AccountEntity account;

	@BeforeEach
	void setUp() {
		users = mock(UserRepository.class);
		accounts = mock(AccountRepository.class);
		transactions = mock(TransactionRepository.class);
		authentication = mock(Authentication.class);
		user = new UserEntity("Asha", "Kumar", "asha@example.com", "hash", "CUSTOMER");
		account = new AccountEntity(user, "AC001", "SAVINGS");
		ReflectionTestUtils.setField(account, "accountId", 1);
		when(authentication.getName()).thenReturn("asha@example.com");
		when(authentication.getAuthorities()).thenReturn(java.util.List.of());
		when(users.findByEmailIgnoreCase("asha@example.com")).thenReturn(Optional.of(user));
		service = new RestBankingService(users, accounts, transactions, mock(BeneficiaryRepository.class),
				new BCryptPasswordEncoder(), mock(AuthenticationManager.class));
	}

	@Test
	void registersUserWithEncodedPassword() {
		when(users.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
		when(users.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.register(new RegisterRequest("New", "User", "new@example.com", "password123"));

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(users).save(captor.capture());
		assertEquals("CUSTOMER", captor.getValue().getRole());
		org.junit.jupiter.api.Assertions.assertTrue(new BCryptPasswordEncoder()
				.matches("password123", captor.getValue().getPasswordHash()));
	}

	@Test
	void rejectsDuplicateEmail() {
		when(users.existsByEmailIgnoreCase("asha@example.com")).thenReturn(true);
		assertThrows(ConflictException.class,
				() -> service.register(new RegisterRequest("Asha", "Kumar", "asha@example.com", "password123")));
	}

	@Test
	void createsAccount() {
		when(accounts.existsByAccountNumberIgnoreCase("AC001")).thenReturn(false);
		when(accounts.save(any(AccountEntity.class))).thenReturn(account);
		assertEquals("AC001", service.createAccount(authentication,
				new CreateAccountRequest("AC001", "SAVINGS")).accountNumber());
	}

	@Test
	void deposits() {
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		service.deposit(authentication, "AC001", new DepositRequest(new BigDecimal("100.00")));
		assertEquals(new BigDecimal("100.00"), account.getBalance());
	}

	@Test
	void withdraws() {
		account.setBalance(new BigDecimal("100.00"));
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		service.withdraw(authentication, "AC001", new WithdrawRequest(new BigDecimal("40.00")));
		assertEquals(new BigDecimal("60.00"), account.getBalance());
	}

	@Test
	void rejectsInsufficientBalance() {
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		assertThrows(BusinessRuleException.class,
				() -> service.withdraw(authentication, "AC001", new WithdrawRequest(BigDecimal.ONE)));
	}

	@Test
	void transfers() {
		UserEntity receiverUser = new UserEntity("Ben", "User", "ben@example.com", "hash", "CUSTOMER");
		AccountEntity receiver = new AccountEntity(receiverUser, "AC002", "SAVINGS");
		ReflectionTestUtils.setField(receiver, "accountId", 2);
		account.setBalance(new BigDecimal("100.00"));
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		when(accounts.findByAccountNumberIgnoreCase("AC002")).thenReturn(Optional.of(receiver));
		service.transfer(authentication, new TransferRequest("AC001", "AC002", new BigDecimal("35.00")));
		assertEquals(new BigDecimal("65.00"), account.getBalance());
		assertEquals(new BigDecimal("35.00"), receiver.getBalance());
	}

	@Test
	void rejectsSameAccountTransfer() {
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		assertThrows(BusinessRuleException.class,
				() -> service.transfer(authentication, new TransferRequest("AC001", "AC001", BigDecimal.ONE)));
	}

	@Test
	void rejectsFrozenAccount() {
		account.setStatus("FROZEN");
		when(accounts.findByAccountNumberIgnoreCase("AC001")).thenReturn(Optional.of(account));
		assertThrows(BusinessRuleException.class,
				() -> service.deposit(authentication, "AC001", new DepositRequest(BigDecimal.ONE)));
	}

	@Test
	void rejectsAnotherUsersAccount() {
		UserEntity other = new UserEntity("Other", "User", "other@example.com", "hash", "CUSTOMER");
		AccountEntity otherAccount = new AccountEntity(other, "AC999", "SAVINGS");
		when(accounts.findByAccountNumberIgnoreCase("AC999")).thenReturn(Optional.of(otherAccount));
		assertThrows(ForbiddenOperationException.class,
				() -> service.getAccount(authentication, "AC999"));
		verify(transactions, never()).save(any());
	}
}
