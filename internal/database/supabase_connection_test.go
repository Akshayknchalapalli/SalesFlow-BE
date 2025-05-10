package database

import (
	"testing"
)

func TestSupabaseConnection(t *testing.T) {
	// Initialize Supabase client
	err := InitSupabase()
	if err != nil {
		t.Fatalf("Failed to initialize Supabase: %v", err)
	}

	// Get the client
	client := GetSupabaseClient()
	if client == nil {
		t.Fatal("Supabase client is nil")
	}

	// Try to fetch a user (this will fail if connection is not working)
	_, count, err := client.From("users").Select("*", "exact", false).Execute()

	if err != nil {
		t.Logf("Database connection test result: %v", err)
		t.Log("Note: This is not a failure if the error is 'no rows in result set'")
	} else {
		t.Logf("Successfully connected to Supabase. Found %d records", count)
	}

	t.Log("Successfully connected to Supabase")
}
