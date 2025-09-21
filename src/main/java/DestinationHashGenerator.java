import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

public class DestinationHashGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <rollNumber> <jsonFilePath>");
            System.exit(1);
        }

        String rollNumber = args[0].toLowerCase().trim();
        String jsonFilePath = args[1];

        try {
            // Read and parse JSON file
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(jsonFilePath));

            // Find the first instance of "destination" key
            String destinationValue = findDestination(rootNode);

            if (destinationValue == null) {
                System.err.println("Key 'destination' not found in the JSON file");
                System.exit(1);
            }

            // Generate random 8-character alphanumeric string
            String randomString = generateRandomString(8);

            // Create concatenated string: rollNumber + destinationValue + randomString
            String concatenatedString = rollNumber + destinationValue + randomString;

            // Generate MD5 hash
            String md5Hash = generateMD5Hash(concatenatedString);

            // Output format: hash;randomString
            System.out.println(md5Hash + ";" + randomString);

        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5 algorithm not available: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Recursively traverse JSON to find the first instance of "destination" key
     */
    private static String findDestination(JsonNode node) {
        if (node == null) {
            return null;
        }

        // If current node is an object, check for "destination" key
        if (node.isObject()) {
            if (node.has("destination")) {
                JsonNode destinationNode = node.get("destination");
                if (destinationNode.isTextual()) {
                    return destinationNode.asText();
                } else if (destinationNode.isValueNode()) {
                    return destinationNode.asText();
                }
            }

            // Recursively search in all fields
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String result = findDestination(node.get(fieldName));
                if (result != null) {
                    return result;
                }
            }
        }
        // If current node is an array, search in all elements
        else if (node.isArray()) {
            for (JsonNode element : node) {
                String result = findDestination(element);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Generate a random alphanumeric string of specified length
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Generate MD5 hash of the input string
     */
    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}