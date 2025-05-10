package database

import (
	"os"
	"testing"
)

func TestInitSupabase(t *testing.T) {
	// Check if environment variables are set
	if os.Getenv("SUPABASE_URL") == "" || os.Getenv("SUPABASE_ANON_KEY") == "" {
		t.Skip("Skipping test: SUPABASE_URL and SUPABASE_ANON_KEY environment variables are not set")
	}

	err := InitSupabase()
	if err != nil {
		t.Fatalf("Failed to initialize Supabase: %v", err)
	}

	client := GetSupabaseClient()
	if client == nil {
		t.Fatal("Supabase client is nil")
	}

	// Test successful initialization
	t.Log("Successfully initialized Supabase client")
}
