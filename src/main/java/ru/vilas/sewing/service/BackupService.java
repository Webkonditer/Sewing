package ru.vilas.sewing.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

@Service
public class BackupService {

    @Value("${backup.directory}")
    private String backupDirectory;

    // Настройки электронной почты
    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.password}")
    private String emailPassword;

    @Value("${mail.to}")
    private String toEmail;
    // Настройки электронной почты для ЯндексПочты

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private String smtpPort;

    @Value("${mail.smtp.auth}")
    private String smtpAuth;

    @Value("${mail.smtp.starttls.enable}")
    private String smtpStartTlsEnable;

    public void createBackupAndSendEmail() {
        String pgDumpPath = "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe";

        // Формирование имени файла резервной копии с учетом времени
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        String backupFileName = "backup_" + timestamp + ".sql";
        // Указываете путь к файлу для сохранения результата
        String output = "D:\\Backup\\backup_" + timestamp + ".sql";
        // Создание резервной копии базы данных с использованием pg_dump
        // параметры команды
        String[] command = {
                pgDumpPath,
                "-U", "postgres",
                "-d", "sewing",
                "-h", "localhost",
                "-p", "5432",
                "-w",
                "-F", "t"
        };
        File outputFile = new File(output);
        // Добавляем перенаправление вывода в файл
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectOutput(outputFile);
        try {
            // Запускаем процесс
            Process process = processBuilder.start();
            // Читаем вывод ошибок
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println("Ошибка pg_dump: " + line);
                }
            }
            // Дожидаемся завершения процесса
            int exitCode = process.waitFor();
            if (exitCode == 0) {

                // Сжимаем и удаляем файл с использованием 7-Zip
                String zipCommand = "C:\\Program Files\\7-Zip\\7z.exe";
                String[] zipArgs = {
                        "a",
                        "-tgzip",
                        "\"" + output + ".gz\"",
                        "\"" + output + "\"",
                        "-sdel"
                };
                ProcessBuilder zipProcessBuilder = new ProcessBuilder(zipCommand);
                zipProcessBuilder.command().addAll(Arrays.asList(zipArgs));
                Process zipProcess = zipProcessBuilder.start();
                zipProcess.waitFor();

                // Получаем новый путь к архивированному файлу
                String zipOutput = output + ".gz";
                File zipOutputFile = new File(zipOutput);
                System.out.println("Бэкап успешно создан и сжат.");

                // Отправляем бэкап по электронной почте
                sendBackupByEmail(zipOutputFile);
            } else {
                System.out.println("Произошла ошибка при создании бэкапа. Код завершения: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendBackupByEmail(File backupFile) {
        // Создаем свойства
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        // Получаем объект Session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        try {
            // Создаем объект MimeMessage
            MimeMessage message = new MimeMessage(session);

            // Устанавливаем отправителя
            message.setFrom(new InternetAddress(fromEmail));

            // Устанавливаем получателя
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Устанавливаем тему письма
            message.setSubject("Бэкап базы данных");

            // Создаем Multipart объект
            Multipart multipart = new MimeMultipart();

            // Добавляем текстовую часть письма (необязательно)
            BodyPart textPart = new MimeBodyPart();
            textPart.setText("Вложение: Бэкап базы данных");
            multipart.addBodyPart(textPart);

            // Добавляем вложение
            BodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(backupFile)));
            attachmentPart.setFileName(backupFile.getName());
            multipart.addBodyPart(attachmentPart);

            // Устанавливаем содержимое письма
            message.setContent(multipart);

            // Отправляем сообщение
            Transport.send(message);

            System.out.println("Бэкап успешно отправлен по электронной почте.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

