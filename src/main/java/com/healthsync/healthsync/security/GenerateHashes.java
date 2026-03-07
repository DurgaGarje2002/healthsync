//package com.healthsync.healthsync.security;
//
//
////Utility: run this in your Spring project to get correct BCrypt hashes
////Add to any @SpringBootTest or run as a simple main() 
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//public class GenerateHashes {
// public static void main(String[] args) {
//     BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
//     
//     System.out.println("=== BCrypt Hashes for HealthSync ===");
//     System.out.println("Test@123  : " + encoder.encode("Test@123"));
//     System.out.println("Admin@123 : " + encoder.encode("Admin@123"));
//     System.out.println("Password1 : " + encoder.encode("Password1"));
//     
//     // Verify existing DB hash
//     String dbHash = "$2a$10$IxEY6Nag0M4w5ZZZxhnKeeBZccakcS1H3hnCmoNWjHoy9VMpqr55y";
//     System.out.println("\nExisting raman hash matches:");
//     System.out.println("  'password'  -> " + encoder.matches("password",  dbHash));
//     System.out.println("  '123456'    -> " + encoder.matches("123456",    dbHash));
//     System.out.println("  'Test@123'  -> " + encoder.matches("Test@123",  dbHash));
//     System.out.println("  'raman'     -> " + encoder.matches("raman",     dbHash));
//     System.out.println("  'Raman@123' -> " + encoder.matches("Raman@123", dbHash));
// }
//}