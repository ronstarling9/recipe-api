# Technical Specification: Adding Quantity Validation to Ingredient Entity                                              
                                                                                                                        
## Issue Description                                                                                                    
The Ingredient entity lacks validation on the quantity field, allowing negative values to be persisted in the database. 
This violates data integrity requirements for recipe ingredients.                                                       
                                                                                                                        
## Solution Overview                                                                                                    
Add validation annotation to the quantity field in the Ingredient entity to ensure quantities are non-negative (≥ 0).   
                                                                                                                        
## Detailed Steps                                                                                                       
                                                                                                                        
### Step 1: Update Ingredient Entity                                                                                    
**File**: `src/main/java/com/rgs/recipeapi/entity/Ingredient.java`                                                      
                                                                                                                        
1. Add import statement for validation annotation:                                                                      
   ```java                                                                                                              
   import jakarta.validation.constraints.Min;                                                                           
                                                                                                                        

 2 Add validation annotation to quantity field:                                                                         
                                                                                                                        
   @Min(0)                                                                                                              
   private Float quantity;                                                                                              
                                                                                                                        

Step 2: Update Unit Test                                                                                                

File: src/test/java/com/rgs/recipeapi/controller/IngredientControllerTest.java                                          

 1 Option A: Remove the problematic test method entirely:                                                               
                                                                                                                        
   // Remove this entire test method:                                                                                   
   @Test                                                                                                                
   void shouldAllowNegativeQuantityOnCreate() throws Exception {                                                        
       // INTENTIONAL BUG: Negative quantities are accepted                                                             
       Ingredient ingredient = new Ingredient();                                                                        
       ingredient.setName("Sugar");                                                                                     
       ingredient.setQuantity(-5.0f);  // Negative!                                                                     
       ingredient.setUnit("cups");                                                                                      
                                                                                                                        
       mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")                                          
                       .contentType(MediaType.APPLICATION_JSON)                                                         
                       .content(objectMapper.writeValueAsString(ingredient)))                                           
               .andExpect(status().isCreated())                                                                         
               .andExpect(jsonPath("$.quantity").value(-5.0));                                                          
   }                                                                                                                    
                                                                                                                        
 2 Option B: Update the test to verify validation error:                                                                
                                                                                                                        
   @Test                                                                                                                
   void shouldRejectNegativeQuantityOnCreate() throws Exception {                                                       
       Ingredient ingredient = new Ingredient();                                                                        
       ingredient.setName("Sugar");                                                                                     
       ingredient.setQuantity(-5.0f);  // Negative!                                                                     
       ingredient.setUnit("cups");                                                                                      
                                                                                                                        
       mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")                                          
                       .contentType(MediaType.APPLICATION_JSON)                                                         
                       .content(objectMapper.writeValueAsString(ingredient)))                                           
               .andExpect(status().isBadRequest());                                                                     
   }                                                                                                                    
                                                                                                                        

Step 3: Verify Application Behavior                                                                                     

 1 Run the application                                                                                                  
 2 Test that negative quantities are rejected with appropriate error response                                           
 3 Test that positive quantities are accepted normally                                                                  
 4 Run all existing tests to ensure no regressions                                                                      

Step 4: Testing Verification                                                                                            

 1 Execute IngredientControllerTest to confirm validation works                                                         
 2 Verify that the API returns proper HTTP status codes (400 for validation errors)                                     
 3 Confirm error messages are appropriately formatted                                                                   


Expected Outcome                                                                                                        

 • Ingredient quantities must be ≥ 0                                                                                    
 • Negative quantities will result in 400 Bad Request response                                                          
 • All existing functionality remains intact                                                                            
 • Database integrity is maintained                                                                                     


Risk Assessment                                                                                                         

 • Low risk: Validation is additive and doesn't change existing behavior for valid data                                 
 • Test updates required to reflect new validation behavior                                                             
 • No breaking changes to API contracts for valid use cases

