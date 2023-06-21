<?php

	session_start();
	
	if (isset($_POST['email']))
	{
		//Udana walidacja? Załóżmy, że tak!
		$wszystko_OK=true;
		
		//Sprawdź poprawność nickname'a
		$nick = $_POST['nick'];
		
		//Sprawdzenie długości nicka
		if ((strlen($nick)<3) || (strlen($nick)>20))
		{
			$wszystko_OK=false;
			$_SESSION['e_nick']="Nick musi posiadać od 3 do 20 znaków!";
		}
		
		if (ctype_alnum($nick)==false)
		{
			$wszystko_OK=false;
			$_SESSION['e_nick']="Nick może składać się tylko z liter i cyfr (bez polskich znaków)";
		}
		
		// Sprawdź poprawność adresu email
		$email = $_POST['email'];
		$emailB = filter_var($email, FILTER_SANITIZE_EMAIL);
		//filer_var sanityzacja pomaga usunac wszelkie niedozwolone znaki. np ł zniknie p
		//po sanizycaji np paweł i zmieni na pawe.jaka zmienna sanityzujemy email, i 
		//pozniej argument czyli jak ..tu filter to mailowych. np paweł zmienia na pawe ...
		//ale to nie jest poprawne bo skraca adres
		
		
		//walidacja poprawnsc adrsu ... emailB!=email..tzn kiedy nie sa rowne sobie np kiedy zostalo skrocone
		// The FILTER_VALIDATE_EMAIL filter validates an e-mail address.
		if ((filter_var($emailB, FILTER_VALIDATE_EMAIL)==false) || ($emailB!=$email))
		{
			$wszystko_OK=false;
			$_SESSION['e_email']="Podaj poprawny adres e-mail!";
		}
		
		//Sprawdź poprawność hasła, tutaj czyl rowna 
		$haslo1 = $_POST['haslo1'];
		$haslo2 = $_POST['haslo2'];
		
		//warunki jesli jest mniejsza od 8 lub wieksza od 20, to false .zmienna e haslo o tresci 'haslo musi...
		//
		if ((strlen($haslo1)<3) || (strlen($haslo1)>20))
		{
			$wszystko_OK=false;
			$_SESSION['e_haslo']="Hasło musi posiadać od 8 do 20 znaków!";
		}
		//sprawdzanie obu hasel czy sa takie same
		if ($haslo1!=$haslo2)
		{
			$wszystko_OK=false;
			$_SESSION['e_haslo']="Podane hasła nie są identyczne!";
		}	
//haslo zahaszowane ta zmienna jestt rowna funkcji password has (co haszujemy ..gaslo 1, 
//zas 2gi argument stala ...password default..) pas default to staa oznaczajaca ..
//uzyj najsilniejszego algorytmu haszujacego jaki jest dostepny

		$haslo_hash = password_hash($haslo1, PASSWORD_DEFAULT);
		
		//Czy zaakceptowano regulamin? jezeli nie jest ustawiona zmienna post regulamin to 
		//bla d e regulami  i tresc potwierdza akcept...
		if (!isset($_POST['regulamin']))
		{
			$wszystko_OK=false;
			$_SESSION['e_regulamin']="Potwierdź akceptację regulaminu!";
		}				
		
		//Bot or not? Oto jest pytanie! tutaj wprowadzilismy z cpaptcha secret key
		$sekret = "6Ld6IiMaAAAAAAyRap9_K1mij6LyAqBuj9XxJ0fI";
		
		//sprawdzamy reakcje google czy captcha sie zhadza ...zmienna psrawdz pobierz zawartosc pliku 
		//adres pliku to google ..podalismi zienna sekret i doklejamy do zmiennej
//		postem przez response o zmienne post ...
//odpowiedz z google bedzie w formacie json
		
		$sprawdz = file_get_contents('https://www.google.com/recaptcha/api/siteverify?secret='.$sekret.'&response='.$_POST['g-recaptcha-response']);
	//zmienna odpowiedz zdekoduj z formatu json..dekodujemy zmienna spradz do odpowiedz 
		$odpowiedz = json_decode($sprawdz);
		
//sprawdzamy czy weryfikacja sie udala czy nie..
//jezeli..zdekodowana odpowiedz sprawdzamy cczy sukces ..prawda czy false
		
		if ($odpowiedz->success==false)
		{
			
//jezeli to nasapilo to nowy erro ebot ..
			$wszystko_OK=false;
			$_SESSION['e_bot']="Potwierdź, że nie jesteś botem!";
		}		
		
		//Zapamiętaj wprowadzone dane, jak psrawic aby formularz rejestracji pamietal wprowadzone dane 
//zmienna e fr..  zapamietuje zmienna np fr nick= nick.... 
//potem te zmienne fr_..
//potem w poszczegolnych zmiennch w bodu iustawiamy zmienne sesyjne
//napjperw srawdzamy przez value czy zmienna sesyjna jest usawione jesli tak 
//to pkozy\ujemy echem i potem likwidujem unset sesja
		$_SESSION['fr_nick'] = $nick;
		$_SESSION['fr_email'] = $email;
		$_SESSION['fr_haslo1'] = $haslo1;
		$_SESSION['fr_haslo2'] = $haslo2;
	  $_SESSION['fr_imie'] = $imie;
	  	  $_SESSION['fr_nazwisko'] = $nazwisko;
$_SESSION['fr_adres'] = $adres;
$_SESSION['fr_wyksztalcenie'] = $wyksztalcenie;
$_SESSION['fr_hobby'] = $hobby;
		
//teraz sprawdzamy zapamietamy akceptacje regulaminu ..sprawdzamy czy zmienna w post wogole istnieej jesli sie spelni to ustawiamy zmienna sesyjna 
//
		if (isset($_POST['regulamin'])) $_SESSION['fr_regulamin'] = true;
//w naszej walidacji email brakuje sprawdzenia czy ktos taki znajduje sie w bazie ni emoze byc duplikatow wartosci
// najpierw lacze sie z baza danych  require ..plik connect w ktorym podalismy na baze danych
//z zaloguj robilimy polaczenie za pomoca malpy
		require_once "connect.php";
		
//ustawiamy sposob raportowania bledo aby nie byla tak widoczny 
//zamiast warningow chcemy pokazywac wyjatki a nie ostrzezenia
		mysqli_report(MYSQLI_REPORT_STRICT);
// proba polaczenia z baza dokonamy za pomoca try
// probuj ....   $db_password
		try 
		{
			$polaczenie = new mysqli($host, $db_user,$db_password, $db_name);
//kiedy polaczenie jest rozne od zera bo zero to jest polaczenie,wowczas  to rzuc nowym wyjatkiem aby sekcja catch zlapalo i 
//wyswietlila go wowczas aby miec pelny dweweloperski opis bledu
			if ($polaczenie->connect_errno!=0)
			{
				throw new Exception(mysqli_connect_errno());
			}
			
//sprawdzamy jesli sie 
			else
			{
//Czy email już istnieje?  rezulta rowna sie polaczenia kwerenda , wynieramy from uzytkownicy tam gdzie emial jest rowny) 
				$rezultat = $polaczenie->query("SELECT id FROM uzytkownicy WHERE email='$email'");
// jezeli rezuktat jest false wowczas rzuc nowym wyjatkiem i pokaz na ekranie polacznie error
//
				if (!$rezultat) throw new Exception($polaczenie->error);
//ile takich maili ..ilosc takich rekordow czyli ile takich adresow email istnieje w bazie
//
				$ile_takich_maili = $rezultat->num_rows;
//jesli wiecej od zerqa, to  blad z info ze istnieje juz takie konto
				if($ile_takich_maili>0)
				{
					$wszystko_OK=false;
					$_SESSION['e_email']="Istnieje już konto przypisane do tego adresu e-mail!";
				}		

				//Czy nick jest już zarezerwowany? czy login sie nie powtarza i np ni ema tacjh w bazie
	//tak samo ale dla nicka 
				$rezultat = $polaczenie->query("SELECT id FROM uzytkownicy WHERE user='$nick'");
				
				if (!$rezultat) throw new Exception($polaczenie->error);
				
				$ile_takich_nickow = $rezultat->num_rows;
				if($ile_takich_nickow>0)
				{
					$wszystko_OK=false;
					$_SESSION['e_nick']="Istnieje już gracz o takim nicku! Wybierz inny.";
				}
			$imie = $_POST['imie'];
			$nazwisko = $_POST['nazwisko'];
			$adres = $_POST['adres'];
			$wyksztalcenie = $_POST['wyksztalcenie'];
			$hobby = $_POST['hobby'];
				if ($wszystko_OK==true)
				{
					//Hurra, wszystkie testy zaliczone, dodajemy gracza do bazy
	//zlapiemy wyjatek czy zapytanie zrobilo prawidlowo
	//polaczenie zapytanie ..instero into ( null..., kazda zmienna ..nick bo to nazwa usera
	//hasz hasla , adres mail, potem surowce ..po 100 i tp bo kazdy takie ptrzymujei 14 dni premium
	//kiedy tak i udalo sie dodac nowego graczas i ustawiamy zmienna sesyjna /..udana rejestr 
	//owczas przekierujemy do strony witamy php ktora utworzymy 
					if ($polaczenie->query("INSERT INTO uzytkownicy VALUES (NULL, '$nick', '$haslo_hash', '$email', '$imie' ,'$nazwisko' , '$adres', '$wyksztalcenie' , '$hobby')"))
					{
						$_SESSION['udanarejestracja']=true;
						header('Location: witamy.php');
					}
//kiedy false to wrzucamy wyjatek 
					else
					{
						throw new Exception($polaczenie->error);
					}
					
				}
	//zamykamy polaczenia na koniec			
				$polaczenie->close();
			}
			
		}
		
//probuj za try ...potem catch , zlap wyjatki zmienna dolar e 
//wowczas napis blad serwera...
//i dodatkowo dla info devel i co jest bledu e..
		catch(Exception $e)
		{
			echo '<span style="color:red;">Błąd serwera! Przepraszamy za niedogodności i prosimy o rejestrację w innym terminie!</span>';
			echo '<br />Informacja developerska: '.$e;
		}
		
	}
	
	
?>

<!DOCTYPE HTML>
<html lang="pl">
<head>
	<meta charset="utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
	<title>Osadnicy - załóż darmowe konto!</title>
	<script src='https://www.google.com/recaptcha/api.js'></script>
	
	<style>
		.error
		{
			color:red;
			margin-top: 10px;
			margin-bottom: 10px;
		}
	</style>
</head>

<body>
	
	<form method="post">
	
		Nickname: <br /> <input type="text" value="<?php
			if (isset($_SESSION['fr_nick']))
			{
				echo $_SESSION['fr_nick'];
				unset($_SESSION['fr_nick']);
			}
		?>" name="nick" /><br />
		
		<?php
			if (isset($_SESSION['e_nick']))
			{
				echo '<div class="error">'.$_SESSION['e_nick'].'</div>';
				unset($_SESSION['e_nick']);
			}
		?>
		
		E-mail: <br /> <input type="text" value="<?php
			if (isset($_SESSION['fr_email']))
			{
				echo $_SESSION['fr_email'];
				unset($_SESSION['fr_email']);
			}
		?>" name="email" /><br />
		
		<?php
			if (isset($_SESSION['e_email']))
			{
				echo '<div class="error">'.$_SESSION['e_email'].'</div>';
				unset($_SESSION['e_email']);
			}
		?>
		
		Twoje hasło: <br /> <input type="password"  value="<?php
			if (isset($_SESSION['fr_haslo1']))
			{
				echo $_SESSION['fr_haslo1'];
				unset($_SESSION['fr_haslo1']);
			}
		?>" name="haslo1" /><br />
		
		<?php
			if (isset($_SESSION['e_haslo']))
			{
				echo '<div class="error">'.$_SESSION['e_haslo'].'</div>';
				unset($_SESSION['e_haslo']);
			}
		?>		
		
		Powtórz hasło: <br /> <input type="password" value="<?php
			if (isset($_SESSION['fr_haslo2']))
			{
				echo $_SESSION['fr_haslo2'];
				unset($_SESSION['fr_haslo2']);
			}
		?>" name="haslo2" /><br />
		
		imie: <br /> <input type="text" value="<?php
			if (isset($_SESSION['fr_imie']))
			{
				echo $_SESSION['fr_imie'];
				unset($_SESSION['fr_imie']);
			}
		?>" name="imie" /><br />
		
		nazwisko: <br /> <input type="text" value="<?php
			if (isset($_SESSION['fr_nazwisko']))
			{
				echo $_SESSION['fr_nazwisko'];
				unset($_SESSION['fr_nazwisko']);
			}
		?>" name="nazwisko" /><br />
		adres: <br /> <input type="text" value="<?php
			if (isset($_SESSION['fr_adres']))
			{
				echo $_SESSION['fr_adres'];
				unset($_SESSION['fr_adres']);
			}
		?>" name="adres" /><br />
		

			
	wyksztalcenie: 	<label  for="wyksztalcenie"><br /></label>
 <select name="wyksztalcenie" >
   <option value="PODSTAWOWE">PODSTAWOWE</option>
   <option value="SREDNIE">SREDNIE</option>
   <option value="WYZSZE">WYZSZE</option>
 </select>   <br />	
  <br />	

	hobby: 	<label  for="hobby"><br /></label>
 <select name="hobby" multiple>
   <option value="CHEMIA">CHEMIA</option>
   <option value="BIOLOGIA">BIOLOGIA</option>
   <option value="FIZYKA">FIZYKA</option>
   <option value="ENERGIA">ENERGIA</option>
   <option value="FUTBOL">FUTBOL</option>
 </select>   <br />	
  <br />


		
		
		<label>
			<input type="checkbox" name="regulamin" <?php
			if (isset($_SESSION['fr_regulamin']))
			{
				echo "checked";
				unset($_SESSION['fr_regulamin']);
			}
				?>/> Akceptuję regulamin
		</label>
		
		<?php
			if (isset($_SESSION['e_regulamin']))
			{
				echo '<div class="error">'.$_SESSION['e_regulamin'].'</div>';
				unset($_SESSION['e_regulamin']);
			}
		?>	
		
		<div class="g-recaptcha" data-sitekey="6Ld6IiMaAAAAAGtxG9heOG1MfX59hYtUQErJWvjl"></div>
		
		<?php
//dodatlimy blad ebot. dodajemy  pokazywanie bledu ebot w formularzu tym pod divem z recaptcha
//
			if (isset($_SESSION['e_bot']))
			{
				echo '<div class="error">'.$_SESSION['e_bot'].'</div>';
				unset($_SESSION['e_bot']);
			}
		?>	
		
		<br />
		
		<input type="submit" value="Zarejestruj się" />
		
	</form>

</body>
</html>