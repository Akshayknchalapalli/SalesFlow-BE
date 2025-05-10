package database

import (
	"fmt"
	"log"
	"os"
	"path/filepath"

	"github.com/joho/godotenv"
	"github.com/supabase-community/supabase-go"
)

var (
	// SupabaseClient is the global Supabase client instance
	SupabaseClient *supabase.Client
)

// InitSupabase initializes the Supabase client with credentials from environment variables
func InitSupabase() error {
	// Try multiple possible locations for the .env file
	possiblePaths := []string{
		// Absolute path
		`C:\Users\aksha\Desktop\SalesFlow-BE\contact-core-service\.env`,
		// Relative path from project root
		filepath.Join("contact-core-service", ".env"),
		// Relative path from internal/database
		filepath.Join("..", "..", "contact-core-service", ".env"),
	}

	var envPath string
	var err error

	// Try each path until we find the .env file
	for _, path := range possiblePaths {
		log.Printf("Trying to load .env from: %s", path)
		if err := godotenv.Load(path); err == nil {
			envPath = path
			break
		}
	}

	if envPath == "" {
		return fmt.Errorf("could not find .env file in any of the expected locations")
	}

	log.Printf("Successfully loaded .env file from: %s", envPath)

	// Get Supabase credentials from environment variables
	supabaseUrl := os.Getenv("SUPABASE_URL")
	supabaseKey := os.Getenv("SUPABASE_ANON_KEY")

	// Log the values (without the actual key for security)
	log.Printf("SUPABASE_URL: %s", supabaseUrl)
	log.Printf("SUPABASE_ANON_KEY length: %d", len(supabaseKey))

	// Validate environment variables
	if supabaseUrl == "" || supabaseKey == "" {
		return fmt.Errorf("missing required environment variables: SUPABASE_URL and SUPABASE_ANON_KEY must be set")
	}

	// Create Supabase client
	client, err := supabase.NewClient(supabaseUrl, supabaseKey, nil)
	if err != nil {
		return fmt.Errorf("failed to create Supabase client: %v", err)
	}

	// Set global client
	SupabaseClient = client
	return nil
}

// GetSupabaseClient returns the initialized Supabase client
func GetSupabaseClient() *supabase.Client {
	if SupabaseClient == nil {
		log.Fatal("Supabase client not initialized. Call InitSupabase() first")
	}
	return SupabaseClient
}

// CloseSupabase closes the Supabase client connection
func CloseSupabase() {
	if SupabaseClient != nil {
		// Add any cleanup code here if needed
		SupabaseClient = nil
	}
}
